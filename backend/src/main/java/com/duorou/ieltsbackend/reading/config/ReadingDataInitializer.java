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
 * 在开发阶段，启动 Spring Boot 时自动检查真实 Reading 数据是否已经导入。
 *
 * 数据来源：
 *
 * src/main/resources/data/reading/reading-p1-high-01.json
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

            /**
             * p1-high-01 是我们真实题库中的 externalId。
             *
             * 如果已经存在：
             * 不再重复导入。
             */
            if (readingTestRepository.existsByExternalId("p1-high-01")) {
                return;
            }

            /**
             * 如果不存在，
             * 就通过 ReadingImportService 读取 JSON，
             * 并保存到 SQLite。
             */
            readingImportService.importReading(
                    "reading-p1-high-01.json"
            );
        };
    }
}
