package com.duorou.ieltsbackend.reading.config;

import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import com.duorou.ieltsbackend.reading.importer.ReadingImportService;
import com.duorou.ieltsbackend.reading.repository.ReadingTestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ReadingDataInitializer
 *
 * 负责在 Spring Boot 启动时初始化 Reading 题库。
 *
 * 当前流程：
 *
 * 1. 先处理旧版 Reading Test 01 -> c21-test-1 的迁移
 *    这样可以尽量保留原来的 ReadingTest 数据库 ID，
 *    避免已有 ReadingPracticeRecord 失去关联。
 *
 * 2. 扫描：
 *
 *    src/main/resources/data/reading/converted/
 *
 *    目录下所有正式 Reading JSON。
 *
 * 3. 根据 externalId 判断：
 *
 *    已存在 -> 跳过
 *    不存在 -> 导入
 *
 * 4. 启动日志最后输出：
 *
 *    Imported
 *    Skipped
 *    Failed
 */
@Configuration
public class ReadingDataInitializer {

    private static final Logger log =
            LoggerFactory.getLogger(
                    ReadingDataInitializer.class
            );

    /**
     * 旧版完整 Reading Test 的 externalId。
     *
     * 这是之前单套题阶段使用的内部名称。
     */
    private static final String OLD_READING_TEST_EXTERNAL_ID =
            "reading-test-01";

    /**
     * 当前批量题库中，
     * 原来 Reading Test 01 对应的新 externalId。
     *
     * 这样迁移后：
     *
     * ReadingTest.id 尽量保持不变
     * externalId 更新为 c21-test-1
     */
    private static final String C21_TEST_1_EXTERNAL_ID =
            "c21-test-1";

    /**
     * converted 目录中的正式 JSON 路径。
     *
     * ReadingImportService.loadReadingFile(...)
     * 会自动在前面加：
     *
     * data/reading/
     *
     * 所以这里只需要写：
     *
     * converted/c21-test-1.json
     */
    private static final String C21_TEST_1_FILE =
            "converted/c21-test-1.json";


    @Bean
    CommandLineRunner initReadingData(
            ReadingImportService readingImportService,
            ReadingTestRepository readingTestRepository
    ) {

        return args -> {

            // =================================================
            // 1. 先处理旧版 Test 01 -> c21-test-1 的迁移
            // =================================================

            migrateOldReadingTestIfNecessary(
                    readingImportService,
                    readingTestRepository
            );


            // =================================================
            // 2. 获取 converted/ 目录下全部正式 JSON
            // =================================================

            List<String> convertedFiles =
                    readingImportService
                            .listConvertedReadingFiles();


            log.info(
                    "Found {} converted Reading files.",
                    convertedFiles.size()
            );


            // =================================================
            // 3. 批量导入
            // =================================================

            int imported = 0;
            int skipped = 0;
            int failed = 0;


            for (String fileName : convertedFiles) {

                try {

                    /**
                     * 先读取 JSON。
                     *
                     * 这里主要是为了拿到 externalId。
                     */
                    String externalId =
                            readingImportService
                                    .loadReadingFile(fileName)
                                    .getExternalId();


                    /**
                     * 数据库已经存在：
                     *
                     * 不重复导入。
                     */
                    if (readingTestRepository
                            .existsByExternalId(
                                    externalId
                            )) {

                        skipped++;

                        log.info(
                                "Skip Reading test: {}",
                                externalId
                        );

                        continue;
                    }


                    /**
                     * 数据库不存在：
                     *
                     * 正常导入。
                     */
                    readingImportService
                            .importReading(fileName);

                    imported++;

                    log.info(
                            "Imported Reading test: {}",
                            externalId
                    );


                } catch (Exception e) {

                    /**
                     * 一套题失败时：
                     *
                     * 当前这套 importReading() 会因为 @Transactional
                     * 自动回滚。
                     *
                     * 然后继续处理下一套，
                     * 不让一套坏数据阻塞整个批量导入。
                     */
                    failed++;

                    log.error(
                            "Failed to import Reading file: {}",
                            fileName,
                            e
                    );
                }
            }


            // =================================================
            // 4. 输出批量导入汇总
            // =================================================

            log.info(
                    "Reading batch import finished. "
                            + "Imported: {}, "
                            + "Skipped: {}, "
                            + "Failed: {}",
                    imported,
                    skipped,
                    failed
            );
        };
    }


    /**
     * 处理旧数据库中的 Reading Test 01。
     *
     * 为什么单独迁移？
     *
     * 因为之前已经有用户练习记录引用旧 ReadingTest。
     *
     * 如果直接新增一个 c21-test-1：
     *
     * 旧 ReadingPracticeRecord
     *     ↓
     * 旧 ReadingTest
     *
     * 新题库
     *     ↓
     * 新 ReadingTest
     *
     * 两者就会分裂。
     *
     * 所以这里优先复用原来的 ReadingTest，
     * 只重建下面的 Passage / Group / Question，
     * 并把 externalId 更新为 c21-test-1。
     */
    private void migrateOldReadingTestIfNecessary(
            ReadingImportService readingImportService,
            ReadingTestRepository readingTestRepository
    ) {

        // -----------------------------------------------------
        // 情况 1：
        // c21-test-1 已经存在。
        //
        // 说明迁移以前已经完成。
        // -----------------------------------------------------

        if (readingTestRepository.existsByExternalId(
                C21_TEST_1_EXTERNAL_ID
        )) {

            log.info(
                    "Reading migration already completed: {}",
                    C21_TEST_1_EXTERNAL_ID
            );

            return;
        }


        // -----------------------------------------------------
        // 情况 2：
        // 找到旧的 reading-test-01。
        //
        // 使用 reimportReadingContent(...)
        // 保留 ReadingTest 本身。
        // -----------------------------------------------------

        ReadingTest oldTest =
                readingTestRepository
                        .findByExternalId(
                                OLD_READING_TEST_EXTERNAL_ID
                        )
                        .orElse(null);


        if (oldTest == null) {

            /**
             * 新数据库没有旧数据时，
             * 不需要做迁移。
             *
             * 后面的批量导入会正常创建 c21-test-1。
             */
            return;
        }


        log.info(
                "Migrating Reading test {} -> {}",
                OLD_READING_TEST_EXTERNAL_ID,
                C21_TEST_1_EXTERNAL_ID
        );


        readingImportService
                .reimportReadingContent(
                        C21_TEST_1_FILE,
                        OLD_READING_TEST_EXTERNAL_ID
                );


        log.info(
                "Reading migration completed: {}",
                C21_TEST_1_EXTERNAL_ID
        );
    }
}
