package com.duorou.ieltsbackend.reading.importer;

import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import com.duorou.ieltsbackend.reading.importer.dto.ReadingImportDto;
import com.duorou.ieltsbackend.reading.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * ReadingImportServiceTest
 *
 * 主要验证三件事：
 *
 * 1. JSON 能不能成功读取成 DTO
 * 2. JSON 能不能成功导入数据库
 * 3. 同一份题库能不能防止重复导入
 */
@SpringBootTest
class ReadingImportServiceTest {

    @Autowired
    private ReadingImportService readingImportService;

    @Autowired
    private ReadingQuestionRepository readingQuestionRepository;

    @Autowired
    private ReadingPassageRepository readingPassageRepository;

    @Autowired
    private ReadingTestRepository readingTestRepository;

    @Autowired
    private QuestionGroupRepository questionGroupRepository;

    @Autowired
    private QuestionOptionRepository questionOptionRepository;

    /**
     * 每个测试开始前清空 Reading 测试数据。
     *
     * 使用 deleteAllInBatch()，
     * 直接发送 DELETE SQL，
     * 不先把 Entity 从数据库读取出来。
     *
     * 这样可以避免数据库里存在损坏关联时，
     * Hibernate 在加载 ReadingQuestion ->
     * ReadingPassage 的过程中抛出异常。
     */
    @BeforeEach
    void cleanDatabase() {

        /**
         * 删除顺序必须从子表开始。
         *
         * 因为：
         *
         * QuestionOption
         *      ↓
         * QuestionGroup
         *      ↓
         * ReadingPassage
         *      ↓
         * ReadingTest
         *
         * 如果先删除父表，
         * 有外键关系时可能失败。
         */

        // ReadingQuestion 依赖 Passage / Group
        readingQuestionRepository.deleteAllInBatch();

        // QuestionOption 依赖 QuestionGroup
        questionOptionRepository.deleteAllInBatch();

        // QuestionGroup 依赖 ReadingPassage
        questionGroupRepository.deleteAllInBatch();

        // Passage 依赖 ReadingTest
        readingPassageRepository.deleteAllInBatch();

        // 最后删除 ReadingTest
        readingTestRepository.deleteAllInBatch();
    }

    /**
     * 测试：
     * JSON -> ReadingImportDto
     */
    @Test
    void shouldLoadReadingJsonFile() {

        ReadingImportDto dto =
                readingImportService.loadReadingFile(
                        "p1-high-01-converted.json"
                );

        assertNotNull(dto);

        assertEquals(
                "p1-high-01",
                dto.getExternalId()
        );

        assertEquals(
                "A Brief History of Tea",
                dto.getTitle()
        );

        assertEquals(
                1,
                dto.getPassages().size()
        );

        /**
         * 一篇 Passage 里应该有两个 QuestionGroup：
         *
         * Group 1 -> MATCHING_HEADINGS
         * Group 2 -> MATCHING_FEATURES
         */
        assertEquals(
                2,
                dto.getPassages()
                        .get(0)
                        .getQuestionGroups()
                        .size()
        );

        assertEquals(
                "MATCHING_HEADINGS",
                dto.getPassages()
                        .get(0)
                        .getQuestionGroups()
                        .get(0)
                        .getQuestionType()
        );

        assertEquals(
                10,
                dto.getPassages()
                        .get(0)
                        .getQuestionGroups()
                        .get(0)
                        .getOptions()
                        .size()
        );

        assertEquals(
                8,
                dto.getPassages()
                        .get(0)
                        .getQuestionGroups()
                        .get(0)
                        .getQuestions()
                        .size()
        );

        assertEquals(
                "MATCHING_FEATURES",
                dto.getPassages()
                        .get(0)
                        .getQuestionGroups()
                        .get(1)
                        .getQuestionType()
        );

        assertEquals(
                7,
                dto.getPassages()
                        .get(0)
                        .getQuestionGroups()
                        .get(1)
                        .getOptions()
                        .size()
        );

        assertEquals(
                5,
                dto.getPassages()
                        .get(0)
                        .getQuestionGroups()
                        .get(1)
                        .getQuestions()
                        .size()
        );

        assertEquals(
                "A",
                dto.getPassages()
                        .get(0)
                        .getQuestionGroups()
                        .get(1)
                        .getQuestions()
                        .get(4)
                        .getCorrectAnswer()
        );
    }

    /**
     * 测试：
     * DTO -> Entity -> Database
     */
    @Test
    void shouldImportReadingIntoDatabase() {

        ReadingTest savedTest =
                readingImportService.importReading(
                        "p1-high-01-converted.json"
                );

        assertNotNull(savedTest.getId());

        assertEquals(
                "A Brief History of Tea",
                savedTest.getTitle()
        );

        assertEquals(
                "p1-high-01",
                savedTest.getExternalId()
        );

        /**
         * 验证真正写入数据库的数据数量。
         */
        assertEquals(
                2,
                questionGroupRepository.count()
        );

        assertEquals(
                17,
                questionOptionRepository.count()
        );

        assertEquals(
                13,
                readingQuestionRepository.count()
        );
    }

    /**
     * 测试：
     * 同一个 externalId 不允许重复导入。
     */
    @Test
    void shouldRejectDuplicateReadingImport() {

        // 第一次导入成功
        readingImportService.importReading(
                "p1-high-01-converted.json"
        );

        // 第二次导入同一份题库，应该抛异常
        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> readingImportService.importReading(
                                "p1-high-01-converted.json"
                        )
                );

        assertEquals(
                "Reading test already imported: p1-high-01",
                exception.getMessage()
        );
    }
}
