package com.duorou.ieltsbackend.reading.config;

import com.duorou.ieltsbackend.reading.importer.ReadingImportService;
import com.duorou.ieltsbackend.reading.repository.ReadingTestRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ReadingDataInitializer
 *
 * 作用：
 * 在开发阶段，Spring Boot 启动完成后，
 * 自动检查 Reading 测试数据是否已经导入。
 *
 * 如果某个 Reading Test 已经存在：
 * 不重复导入。
 *
 * 如果不存在：
 * 通过 ReadingImportService 从 resources/data/reading/
 * 读取对应 JSON，并写入 SQLite。
 *
 * 当前会检查两份 Reading 数据：
 *
 * 1. reading-p1-high-01.json
 * 2. reading-yasige-test.json
 *
 * 数据流：
 *
 * JSON
 *   ↓
 * ReadingImportService
 *   ↓
 * ReadingTest
 *   ↓
 * ReadingPassage
 *   ↓
 * QuestionGroup
 *   ↓
 * ReadingQuestion
 *   ↓
 * SQLite
 */
@Configuration
public class ReadingDataInitializer {

    /**
     * Spring Boot 启动完成后，
     * CommandLineRunner 会自动执行一次。
     */
    @Bean
    CommandLineRunner initReadingData(
            ReadingImportService readingImportService,
            ReadingTestRepository readingTestRepository
    ) {

        return args -> {

            // =================================================
            // 1. 导入原来的 Reading 测试数据
            // =================================================

            /**
             * p1-high-01
             *
             * 这是原来 reading-p1-high-01.json
             * 对应的 externalId。
             *
             * 如果数据库中不存在，
             * 就执行导入。
             */
            if (!readingTestRepository.existsByExternalId(
                    "p1-high-01"
            )) {

                readingImportService.importReading(
                        "reading-p1-high-01.json"
                );
            }

            // =================================================
            // 2. 导入新转换的完整 Reading Test
            // =================================================

            /**
             * 这个 externalId 来自
             * yasige_to_reading_import.py 生成的 JSON：
             *
             * "externalId": "yasige-4202607160914251713"
             *
             * 如果数据库中不存在，
             * 就导入：
             *
             * reading-yasige-test.json
             */
            if (!readingTestRepository.existsByExternalId(
                    "yasige-4202607160914251713"
            )) {

                readingImportService.importReading(
                        "reading-yasige-test.json"
                );
            }
        };
    }
}
