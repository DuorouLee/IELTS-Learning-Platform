package com.duorou.ieltsbackend.reading.importer;

import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import com.duorou.ieltsbackend.reading.importer.dto.ReadingImportDto;
import com.duorou.ieltsbackend.reading.repository.ReadingPassageRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingQuestionRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingTestRepository;
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

        // 先删最下层 Question
        readingQuestionRepository.deleteAllInBatch();

        // 再删 Passage
        readingPassageRepository.deleteAllInBatch();

        // 最后删 Test
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
                        "reading-p1-high-01.json"
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

        assertEquals(
                2,
                dto.getPassages()
                        .get(0)
                        .getQuestions()
                        .size()
        );

        assertEquals(
                "viii",
                dto.getPassages()
                        .get(0)
                        .getQuestions()
                        .get(0)
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
                        "reading-p1-high-01.json"
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
    }

    /**
     * 测试：
     * 同一个 externalId 不允许重复导入。
     */
    @Test
    void shouldRejectDuplicateReadingImport() {

        // 第一次导入成功
        readingImportService.importReading(
                "reading-p1-high-01.json"
        );

        // 第二次导入同一份题库，应该抛异常
        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> readingImportService.importReading(
                                "reading-p1-high-01.json"
                        )
                );

        assertEquals(
                "Reading test already imported: p1-high-01",
                exception.getMessage()
        );
    }
}
