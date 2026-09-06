package com.duorou.ieltsbackend.reading.importer;

import com.duorou.ieltsbackend.reading.entity.ReadingPassage;
import com.duorou.ieltsbackend.reading.entity.ReadingQuestion;
import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import com.duorou.ieltsbackend.reading.importer.dto.ReadingImportDto;
import com.duorou.ieltsbackend.reading.importer.dto.ReadingPassageImportDto;
import com.duorou.ieltsbackend.reading.importer.dto.ReadingQuestionImportDto;
import com.duorou.ieltsbackend.reading.repository.ReadingPassageRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingQuestionRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingTestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * ReadingImportService
 *
 * 负责两件事：
 *
 * 1. 读取 JSON 文件
 * 2. 把 DTO 转成 Entity 并保存数据库
 */
@Service
public class ReadingImportService {

    private final ObjectMapper objectMapper;

    private final ReadingTestRepository readingTestRepository;
    private final ReadingPassageRepository readingPassageRepository;
    private final ReadingQuestionRepository readingQuestionRepository;

    /**
     * 构造器注入。
     *
     * Spring 会自动把这些对象传进来。
     */
    public ReadingImportService(
            ObjectMapper objectMapper,
            ReadingTestRepository readingTestRepository,
            ReadingPassageRepository readingPassageRepository,
            ReadingQuestionRepository readingQuestionRepository
    ) {
        this.objectMapper = objectMapper;
        this.readingTestRepository = readingTestRepository;
        this.readingPassageRepository = readingPassageRepository;
        this.readingQuestionRepository = readingQuestionRepository;
    }

    /**
     * 读取 resources/data/reading/ 下的 JSON 文件。
     */
    public ReadingImportDto loadReadingFile(String fileName) {

        try {
            ClassPathResource resource =
                    new ClassPathResource(
                            "data/reading/" + fileName
                    );

            return objectMapper.readValue(
                    resource.getInputStream(),
                    ReadingImportDto.class
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to load Reading file: " + fileName,
                    e
            );
        }
    }

    /**
     * 把一份 Reading JSON 真正导入数据库。
     *
     * 数据流：
     *
     * JSON
     *   ↓
     * ReadingImportDto
     *   ↓
     * ReadingTest
     *   ↓
     * ReadingPassage
     *   ↓
     * ReadingQuestion
     *   ↓
     * SQLite
     */
    public ReadingTest importReading(String fileName) {

        // 第一步：
        // 先把 JSON 转成 DTO。
        ReadingImportDto dto =
                loadReadingFile(fileName);


        // =========================
        // 1. 创建 ReadingTest
        // =========================

        ReadingTest test = new ReadingTest();

        test.setTitle(dto.getTitle());
        test.setSource(dto.getSource());

        // 先保存 ReadingTest，
        // 这样数据库会生成 test.id。
        ReadingTest savedTest =
                readingTestRepository.save(test);


        // =========================
        // 2. 遍历所有 Passage
        // =========================

        for (ReadingPassageImportDto passageDto
                : dto.getPassages()) {

            ReadingPassage passage =
                    new ReadingPassage();

            passage.setPassageNumber(
                    passageDto.getPassageNumber()
            );

            passage.setTitle(
                    passageDto.getTitle()
            );

            passage.setContent(
                    passageDto.getContent()
            );

            /**
             * 建立关系：
             *
             * ReadingTest
             *     ↓
             * ReadingPassage
             */
            passage.setReadingTest(savedTest);

            ReadingPassage savedPassage =
                    readingPassageRepository.save(passage);


            // =========================
            // 3. 遍历当前 Passage 的 Questions
            // =========================

            for (ReadingQuestionImportDto questionDto
                    : passageDto.getQuestions()) {

                ReadingQuestion question =
                        new ReadingQuestion();

                question.setQuestionNumber(
                        questionDto.getQuestionNumber()
                );

                question.setQuestionType(
                        questionDto.getQuestionType()
                );

                question.setQuestionText(
                        questionDto.getQuestionText()
                );

                question.setCorrectAnswer(
                        questionDto.getCorrectAnswer()
                );

                question.setExplanation(
                        questionDto.getExplanation()
                );

                /**
                 * 建立关系：
                 *
                 * ReadingPassage
                 *     ↓
                 * ReadingQuestion
                 */
                question.setReadingPassage(
                        savedPassage
                );

                readingQuestionRepository.save(
                        question
                );
            }
        }

        // 最后返回已经保存的 ReadingTest。
        return savedTest;
    }
}
