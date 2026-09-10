package com.duorou.ieltsbackend.reading.importer;

import com.duorou.ieltsbackend.reading.entity.QuestionGroup;
import com.duorou.ieltsbackend.reading.entity.QuestionOption;
import com.duorou.ieltsbackend.reading.entity.ReadingPassage;
import com.duorou.ieltsbackend.reading.entity.ReadingQuestion;
import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import com.duorou.ieltsbackend.reading.importer.dto.QuestionGroupImportDto;
import com.duorou.ieltsbackend.reading.importer.dto.QuestionOptionImportDto;
import com.duorou.ieltsbackend.reading.importer.dto.ReadingImportDto;
import com.duorou.ieltsbackend.reading.importer.dto.ReadingPassageImportDto;
import com.duorou.ieltsbackend.reading.importer.dto.ReadingQuestionImportDto;
import com.duorou.ieltsbackend.reading.repository.QuestionGroupRepository;
import com.duorou.ieltsbackend.reading.repository.QuestionOptionRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingPassageRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingQuestionRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingTestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * ReadingImportService
 *
 * 负责 Reading 题库 JSON 的导入。
 *
 * 当前支持两种情况：
 *
 * 1. importReading(...)
 *
 *    第一次导入一整套 Reading Test。
 *
 *
 * 2. reimportReadingContent(...)
 *
 *    开发阶段重新导入已经存在的 Reading Test 内容。
 *
 *    注意：
 *    这个方法不会删除 ReadingTest 本身。
 *
 *    它只删除并重新建立：
 *
 *    ReadingPassage
 *    QuestionGroup
 *    QuestionOption
 *    ReadingQuestion
 *
 *    这样 ReadingPracticeRecord 仍然可以继续指向
 *    原来的 ReadingTest。
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
     * Spring Boot 会自动把这些 Repository
     * 和 ObjectMapper 注入进来。
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
     * 读取：
     *
     * src/main/resources/data/reading/
     *
     * 目录下的 JSON 文件。
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
     * =========================================================
     * 第一次导入 Reading Test
     * =========================================================
     *
     * 这个方法用于：
     *
     * 数据库中还不存在这套题的时候。
     *
     * 如果 externalId 已经存在，
     * 就阻止重复创建 ReadingTest。
     */
    @Transactional
    public ReadingTest importReading(String fileName) {

        // -----------------------------------------------------
        // 1. 读取 JSON
        // -----------------------------------------------------

        ReadingImportDto dto =
                loadReadingFile(fileName);


        // -----------------------------------------------------
        // 2. 防止重复导入同一个 Reading Test
        // -----------------------------------------------------

        if (readingTestRepository.existsByExternalId(
                dto.getExternalId()
        )) {

            throw new IllegalStateException(
                    "Reading test already imported: "
                            + dto.getExternalId()
            );
        }


        // -----------------------------------------------------
        // 3. 创建 ReadingTest
        // -----------------------------------------------------

        ReadingTest test = new ReadingTest();

        test.setExternalId(
                dto.getExternalId()
        );

        test.setTitle(
                dto.getTitle()
        );

        test.setSource(
                dto.getSource()
        );


        // save() 后数据库生成 Test ID。
        ReadingTest savedTest =
                readingTestRepository.save(test);


        // -----------------------------------------------------
        // 4. 保存 Test 下面的 Passage / Group / Question
        // -----------------------------------------------------

        saveReadingContent(
                dto,
                savedTest
        );


        return savedTest;
    }


    /**
     * 开发阶段重新导入一套已经存在的 Reading Test。
     *
     * fileName：
     * 新版本 JSON 文件名。
     *
     * existingExternalId：
     * 数据库里这套 Test 目前正在使用的 externalId。
     *
     * 为什么需要两个值？
     *
     * 因为这次我们同时进行了内部命名清理：
     *
     * 数据库旧值
     *     ↓
     * existingExternalId
     *
     * 新 JSON
     *     ↓
     * reading-test-01
     *
     * 重导完成以后：
     *
     * ReadingTest 的数据库 ID 不变，
     * 所以 ReadingPracticeRecord 仍然关联原来的 Test。
     *
     * 但 externalId 会更新成 JSON 中的新值。
     */
    @Transactional
    public ReadingTest reimportReadingContent(
            String fileName,
            String existingExternalId
    ) {

        // =====================================================
        // 1. 读取新版 Reading JSON
        // =====================================================

        ReadingImportDto dto =
                loadReadingFile(fileName);


        // =====================================================
        // 2. 找到数据库里原来的 ReadingTest
        // =====================================================
        //
        // 注意：
        // 这里使用 existingExternalId，
        // 而不是 dto.getExternalId()。
        //
        // 因为数据库目前还是旧 externalId。
        // =====================================================

        ReadingTest existingTest =
                readingTestRepository
                        .findByExternalId(
                                existingExternalId
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "Reading test not found for reimport: "
                                                + existingExternalId
                                )
                        );


        Long testId =
                existingTest.getId();


        // =====================================================
        // 3. 删除 Test 下面的旧题库内容
        // =====================================================
        //
        // 必须从依赖链最下面开始删除：
        //
        // QuestionOption
        // ↓
        // ReadingQuestion
        // ↓
        // QuestionGroup
        // ↓
        // ReadingPassage
        //
        // ReadingTest 自己不删除。
        // =====================================================

        questionOptionRepository
                .deleteByQuestionGroupReadingPassageReadingTestId(
                        testId
                );

        readingQuestionRepository
                .deleteByReadingPassageReadingTestId(
                        testId
                );

        questionGroupRepository
                .deleteByReadingPassageReadingTestId(
                        testId
                );

        readingPassageRepository
                .deleteByReadingTestId(
                        testId
                );


        // =====================================================
        // 4. 更新 ReadingTest 自己的信息
        // =====================================================
        //
        // 数据库 Test ID 不会发生变化。
        //
        // 这里只更新：
        // externalId
        // title
        // source
        // =====================================================

        existingTest.setExternalId(
                dto.getExternalId()
        );

        existingTest.setTitle(
                dto.getTitle()
        );

        existingTest.setSource(
                dto.getSource()
        );


        ReadingTest savedTest =
                readingTestRepository.save(
                        existingTest
                );


        // =====================================================
        // 5. 根据新版 JSON 重建 Passage / Group / Question
        // =====================================================

        saveReadingContent(
                dto,
                savedTest
        );


        return savedTest;
    }


    /**
     * =========================================================
     * 保存 ReadingTest 下面的实际题库内容
     * =========================================================
     *
     * 这个方法同时被：
     *
     * importReading()
     *
     * 和：
     *
     * reimportReadingContent()
     *
     * 使用。
     *
     * 这样就不用复制两份很长的 Passage / Question 导入代码。
     */
    private void saveReadingContent(
            ReadingImportDto dto,
            ReadingTest savedTest
    ) {

        // =====================================================
        // 遍历所有 Passage
        // =====================================================

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

            passage.setInstruction(
                    passageDto.getInstruction()
            );

            passage.setContent(
                    passageDto.getContent()
            );

            passage.setTranslation(
                    passageDto.getTranslation()
            );


            /**
             * 建立：
             *
             * ReadingPassage
             *      ↓
             * ReadingTest
             */
            passage.setReadingTest(
                    savedTest
            );


            ReadingPassage savedPassage =
                    readingPassageRepository.save(
                            passage
                    );


            // =================================================
            // QuestionGroup
            // =================================================

            if (passageDto.getQuestionGroups() != null) {

                for (QuestionGroupImportDto groupDto
                        : passageDto.getQuestionGroups()) {

                    // -----------------------------------------
                    // 创建 QuestionGroup
                    // -----------------------------------------

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
                     * JSON 没写 allowOptionReuse 时，
                     * 默认 false。
                     */
                    group.setAllowOptionReuse(
                            Boolean.TRUE.equals(
                                    groupDto.getAllowOptionReuse()
                            )
                    );


                    QuestionGroup savedGroup =
                            questionGroupRepository.save(
                                    group
                            );


                    // =========================================
                    // QuestionOption
                    // =========================================

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


                    // =========================================
                    // ReadingQuestion
                    // =========================================

                    if (groupDto.getQuestions() != null) {

                        for (ReadingQuestionImportDto questionDto
                                : groupDto.getQuestions()) {

                            ReadingQuestion question =
                                    createReadingQuestion(
                                            questionDto,
                                            savedPassage
                                    );


                            /**
                             * 保存当前题所属的 Group ID。
                             */
                            question.setGroupId(
                                    savedGroup.getId()
                            );


                            readingQuestionRepository.save(
                                    question
                            );
                        }
                    }
                }
            }


            // =================================================
            // 兼容旧版 JSON
            // =================================================
            //
            // 老数据：
            //
            // Passage
            //    ↓
            // questions
            //
            // 没有 QuestionGroup。
            // =================================================

            if (passageDto.getQuestions() != null) {

                for (ReadingQuestionImportDto questionDto
                        : passageDto.getQuestions()) {

                    ReadingQuestion question =
                            createReadingQuestion(
                                    questionDto,
                                    savedPassage
                            );


                    readingQuestionRepository.save(
                            question
                    );
                }
            }
        }
    }


    /**
     * =========================================================
     * 根据 DTO 创建 ReadingQuestion
     * =========================================================
     *
     * 这里统一处理：
     *
     * questionNumber
     * questionType
     * questionText
     * correctAnswer
     * explanation
     * options
     * answerHighlight
     *
     * 避免新版、旧版 JSON 重复写同一套代码。
     */
    private ReadingQuestion createReadingQuestion(
            ReadingQuestionImportDto questionDto,
            ReadingPassage savedPassage
    ) {

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
         * Question 仍然属于 Passage。
         */
        question.setReadingPassage(
                savedPassage
        );


        // =====================================================
        // Multiple Choice 独立 options
        // =====================================================

        if (questionDto.getOptions() != null) {

            try {

                question.setOptionsJson(
                        objectMapper.writeValueAsString(
                                questionDto.getOptions()
                        )
                );

            } catch (IOException e) {

                throw new IllegalStateException(
                        "Failed to serialize Reading question options: "
                                + questionDto.getQuestionNumber(),
                        e
                );
            }
        }


        // =====================================================
        // 原文答案高亮
        // =====================================================

        if (questionDto.getAnswerHighlight() != null) {

            try {

                question.setAnswerHighlightJson(
                        objectMapper.writeValueAsString(
                                questionDto.getAnswerHighlight()
                        )
                );

            } catch (IOException e) {

                throw new IllegalStateException(
                        "Failed to serialize answer highlight for question: "
                                + questionDto.getQuestionNumber(),
                        e
                );
            }
        }


        return question;
    }
}
