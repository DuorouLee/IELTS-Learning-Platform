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
import com.duorou.ieltsbackend.reading.entity.QuestionGroup;
import com.duorou.ieltsbackend.reading.entity.QuestionOption;

import com.duorou.ieltsbackend.reading.importer.dto.QuestionGroupImportDto;
import com.duorou.ieltsbackend.reading.importer.dto.QuestionOptionImportDto;

import com.duorou.ieltsbackend.reading.repository.QuestionGroupRepository;
import com.duorou.ieltsbackend.reading.repository.QuestionOptionRepository;


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
    private final QuestionGroupRepository questionGroupRepository;
    private final QuestionOptionRepository questionOptionRepository;

    /**
     * 构造器注入。
     *
     * Spring 会自动把这些对象传进来。
     */
    public ReadingImportService(
            ObjectMapper objectMapper,
            ReadingTestRepository readingTestRepository,
            ReadingPassageRepository readingPassageRepository,
            ReadingQuestionRepository readingQuestionRepository,
            QuestionGroupRepository questionGroupRepository,
            QuestionOptionRepository questionOptionRepository
    ) {
        this.objectMapper = objectMapper;
        this.readingTestRepository = readingTestRepository;
        this.readingPassageRepository = readingPassageRepository;
        this.readingQuestionRepository = readingQuestionRepository;
        this.questionGroupRepository = questionGroupRepository;
        this.questionOptionRepository = questionOptionRepository;
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

        /**
         * 防止重复导入。
         *
         * 如果数据库已经存在相同 externalId，
         * 就停止导入。
         */
        if (readingTestRepository.existsByExternalId(dto.getExternalId())) {
            throw new IllegalStateException(
                    "Reading test already imported: " + dto.getExternalId()
            );
        }

        // =========================
        // 1. 创建 ReadingTest
        // =========================

        ReadingTest test = new ReadingTest();

        test.setExternalId(dto.getExternalId());
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
// 3. 导入 QuestionGroup
// =========================

/**
 * 新 JSON 结构：
 *
 * Passage
 *    ↓
 * QuestionGroup
 *    ├── QuestionOption
 *    └── ReadingQuestion
 *
 * 注意：
 * 老 JSON 可能没有 questionGroups，
 * 所以这里必须先判断 null。
 */
            if (passageDto.getQuestionGroups() != null) {

                for (QuestionGroupImportDto groupDto
                        : passageDto.getQuestionGroups()) {

                    // ---------------------------------
                    // 3.1 创建 QuestionGroup
                    // ---------------------------------

                    QuestionGroup group =
                            new QuestionGroup();

                    group.setReadingPassage(
                            savedPassage
                    );

                    group.setQuestionType(
                            groupDto.getQuestionType()
                    );

                    group.setInstruction(
                            groupDto.getInstruction()
                    );

                    /**
                     * JSON 如果没有写 allowOptionReuse，
                     * 默认按 false 处理。
                     *
                     * 防止数据库 NOT NULL 字段出现 null。
                     */
                    group.setAllowOptionReuse(
                            Boolean.TRUE.equals(
                                    groupDto.getAllowOptionReuse()
                            )
                    );

                    QuestionGroup savedGroup =
                            questionGroupRepository.save(group);


                    // ---------------------------------
                    // 3.2 保存 QuestionOption
                    // ---------------------------------

                    if (groupDto.getOptions() != null) {

                        for (QuestionOptionImportDto optionDto
                                : groupDto.getOptions()) {

                            QuestionOption option =
                                    new QuestionOption();

                            option.setQuestionGroup(
                                    savedGroup
                            );

                            option.setOptionValue(
                                    optionDto.getOptionValue()
                            );

                            option.setOptionText(
                                    optionDto.getOptionText()
                            );

                            option.setDisplayOrder(
                                    optionDto.getDisplayOrder()
                            );

                            questionOptionRepository.save(
                                    option
                            );
                        }
                    }


                    // ---------------------------------
                    // 3.3 保存当前 QuestionGroup 的 Questions
                    // ---------------------------------

                    /**
                     * 兼容旧 JSON。
                     *
                     * 老题库结构：
                     *
                     * Passage
                     *    ↓
                     * Questions
                     *
                     * 这些 Question 没有 QuestionGroup，
                     * 所以 groupId 会保持 null。
                     */
                    if (passageDto.getQuestions() != null) {

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

                            question.setReadingPassage(
                                    savedPassage
                            );

                            readingQuestionRepository.save(
                                    question
                            );
                        }
                    }
                }
            }

            // =========================
            // 4. 遍历当前 Passage 的 Questions
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
