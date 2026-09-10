package com.duorou.ieltsbackend.reading.config;

import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import com.duorou.ieltsbackend.reading.importer.ReadingImportService;
import com.duorou.ieltsbackend.reading.repository.ReadingTestRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ReadingDataInitializer
 *
 * 开发阶段用于初始化 Reading 数据。
 *
 * 当前包含：
 *
 * 1. 原来的单篇 Reading 测试
 * 2. 完整 Reading Test 01
 *
 * 如果数据已经存在，就不会重复导入。
 */
@Configuration
public class ReadingDataInitializer {

    /**
     * 当前完整 Reading Test 在本地数据库中的 ID。
     *
     * 这只是为了完成一次开发阶段的数据迁移。
     *
     * 迁移完成以后：
     *
     * externalId = reading-test-01
     *
     * 后续初始化就只依赖 externalId，
     * 不再依赖这个数据库 ID。
     */
    private static final Long READING_TEST_01_DATABASE_ID = 8L;

    /**
     * 新的内部唯一标识。
     */
    private static final String READING_TEST_01_EXTERNAL_ID =
            "reading-test-01";

    /**
     * 新的 Reading JSON 文件。
     */
    private static final String READING_TEST_01_FILE =
            "reading-test-01.json";


    @Bean
    CommandLineRunner initReadingData(
            ReadingImportService readingImportService,
            ReadingTestRepository readingTestRepository
    ) {

        return args -> {

            // =================================================
            // 1. 保留原来的单篇 Reading 测试
            // =================================================

            if (!readingTestRepository.existsByExternalId(
                    "p1-high-01"
            )) {

                readingImportService.importReading(
                        "reading-p1-high-01.json"
                );
            }


            // =================================================
            // 2. 完整 Reading Test
            // =================================================

            /**
             * 第一种情况：
             *
             * 新 externalId 已经存在。
             *
             * 说明迁移已经完成，
             * 不需要再次导入。
             */
            if (readingTestRepository.existsByExternalId(
                    READING_TEST_01_EXTERNAL_ID
            )) {
                return;
            }


            /**
             * 第二种情况：
             *
             * 新 externalId 还不存在，
             * 但数据库中的 Test 8 已经存在。
             *
             * 说明这是第一次运行新版数据。
             *
             * 此时：
             *
             * 1. 保留 ReadingTest ID = 8
             * 2. 删除下面旧 Passage / Group / Question
             * 3. 更新 externalId
             * 4. 根据 reading-test-01.json 重建内容
             *
             * 这样历史 ReadingPracticeRecord
             * 仍然继续引用 Test 8。
             */
            ReadingTest existingTest =
                    readingTestRepository
                            .findById(
                                    READING_TEST_01_DATABASE_ID
                            )
                            .orElse(null);

            if (existingTest != null) {

                readingImportService.reimportReadingContent(
                        READING_TEST_01_FILE,
                        existingTest.getExternalId()
                );

                return;
            }


            /**
             * 第三种情况：
             *
             * 数据库里连 Test 8 都不存在。
             *
             * 例如：
             * - 新数据库
             * - 第一次启动项目
             *
             * 直接正常导入即可。
             */
            readingImportService.importReading(
                    READING_TEST_01_FILE
            );
        };
    }
}
