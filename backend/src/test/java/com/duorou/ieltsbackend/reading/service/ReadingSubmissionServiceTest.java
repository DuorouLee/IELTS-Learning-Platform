package com.duorou.ieltsbackend.reading.service;

import com.duorou.ieltsbackend.reading.dto.ReadingSubmitRequest;
import com.duorou.ieltsbackend.reading.dto.ReadingSubmitResponse;
import com.duorou.ieltsbackend.reading.entity.ReadingPassage;
import com.duorou.ieltsbackend.reading.entity.ReadingQuestion;
import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import com.duorou.ieltsbackend.reading.repository.ReadingPassageRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingQuestionRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingTestRepository;
import com.duorou.ieltsbackend.reading.entity.ReadingPracticeAnswer;
import com.duorou.ieltsbackend.reading.repository.ReadingPracticeAnswerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import com.duorou.ieltsbackend.reading.exception.ReadingTestNotFoundException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ReadingSubmissionService 集成测试。
 * <p>
 * 这个测试会真实走：
 * <p>
 * ReadingSubmissionService
 * ↓
 * ReadingTestRepository
 * ↓
 * ReadingQuestionRepository
 * ↓
 * Hibernate / JPA
 * ↓
 * SQLite
 * <p>
 * 目的：
 * 验证用户提交答案以后，
 * ReadingSubmissionService 能不能正确完成判分。
 */
@SpringBootTest
@Transactional
class ReadingSubmissionServiceTest {

    /**
     * 真正需要测试的 Service。
     */
    @Autowired
    private ReadingSubmissionService readingSubmissionService;

    /**
     * 下面三个 Repository 主要用于准备测试数据。
     * <p>
     * 注意：
     * 测试的重点仍然是 ReadingSubmissionService，
     * Repository 只是帮助我们建立：
     * <p>
     * ReadingTest
     * ↓
     * ReadingPassage
     * ↓
     * ReadingQuestion
     * <p>
     * 这一套真实数据库数据。
     */
    @Autowired
    private ReadingTestRepository readingTestRepository;

    @Autowired
    private ReadingPassageRepository readingPassageRepository;

    @Autowired
    private ReadingQuestionRepository readingQuestionRepository;

    /**
     * 用于验证 submit 后，
     * 每一道题的历史答案是否真的保存到了数据库。
     */
    @Autowired
    private ReadingPracticeAnswerRepository readingPracticeAnswerRepository;

    /**
     * 每个测试开始前清理相关测试数据，
     * 保证不同测试之间互不影响。
     *
     * 删除顺序要从子表到父表，
     * 避免外键约束问题。
     */
    @BeforeEach
    void setUp() {
        readingPracticeAnswerRepository.deleteAll();
        readingQuestionRepository.deleteAll();
        readingPassageRepository.deleteAll();
        readingTestRepository.deleteAll();
    }

    /**
     * 测试场景：
     * <p>
     * 当前 Test 有两道题：
     * <p>
     * Question 1 正确答案：viii
     * Question 2 正确答案：D
     * <p>
     * 用户两道题全部答对。
     * <p>
     * 预期结果：
     * <p>
     * totalQuestions = 2
     * correctCount = 2
     * incorrectCount = 0
     * percentage = 100.0
     */
    @Test
    void shouldScoreAllAnswersCorrectly() {

        /*
         * ============================================================
         * 第一步：创建 ReadingTest
         * ============================================================
         */
        ReadingTest readingTest = new ReadingTest();

        readingTest.setTitle("Reading Submission Test");
        readingTest.setSource("Integration Test");

        /*
         * save() 以后数据库会生成 ReadingTest id。
         */
        ReadingTest savedTest =
                readingTestRepository.save(readingTest);


        /*
         * ============================================================
         * 第二步：创建 ReadingPassage
         * ============================================================
         *
         * ReadingPassage 必须属于一个 ReadingTest，
         * 因为实体中：
         *
         * @ManyToOne
         * @JoinColumn(name = "test_id", nullable = false)
         *
         * 所以 readingTest 不能是 null。
         */
        ReadingPassage passage = new ReadingPassage();

        passage.setReadingTest(savedTest);

        /*
         * passage_number 在数据库里是 NOT NULL，
         * 所以必须设置。
         */
        passage.setPassageNumber(1);

        passage.setTitle("Test Passage");

        /*
         * content 在 ReadingPassage Entity 中是 nullable = false，
         * 因此测试中也必须提供正文。
         */
        passage.setContent("This is a test reading passage.");

        ReadingPassage savedPassage =
                readingPassageRepository.save(passage);


        /*
         * ============================================================
         * 第三步：创建 Question 1
         * ============================================================
         */
        ReadingQuestion question1 = new ReadingQuestion();

        /*
         * Question 必须属于 Passage。
         */
        question1.setReadingPassage(savedPassage);

        question1.setQuestionNumber(1);

        question1.setQuestionType("MATCHING_HEADINGS");

        question1.setQuestionText("Paragraph A");

        /*
         * 数据库中 correct_answer 是 NOT NULL。
         */
        question1.setCorrectAnswer("viii");

        ReadingQuestion savedQuestion1 =
                readingQuestionRepository.save(question1);


        /*
         * ============================================================
         * 第四步：创建 Question 2
         * ============================================================
         */
        ReadingQuestion question2 = new ReadingQuestion();

        question2.setReadingPassage(savedPassage);
        question2.setQuestionNumber(2);
        question2.setQuestionType("MATCHING_FEATURES");
        question2.setQuestionText("Example matching question");
        question2.setCorrectAnswer("D");

        ReadingQuestion savedQuestion2 =
                readingQuestionRepository.save(question2);


        /*
         * ============================================================
         * 第五步：模拟前端提交答案
         * ============================================================
         *
         * 前端实际发送的数据结构类似：
         *
         * {
         *   "answers": {
         *     "27": "viii",
         *     "35": "D"
         *   }
         * }
         *
         * 测试里不能提前知道数据库生成的 questionId，
         * 所以使用：
         *
         * savedQuestion1.getId()
         * savedQuestion2.getId()
         */
        Map<Long, String> answers = new HashMap<>();

        answers.put(
                savedQuestion1.getId(),
                "viii"
        );

        answers.put(
                savedQuestion2.getId(),
                "D"
        );


        /*
         * 创建前端提交 DTO。
         */
        ReadingSubmitRequest request =
                new ReadingSubmitRequest();

        request.setAnswers(answers);


        /*
         * ============================================================
         * 第六步：真正调用 ReadingSubmissionService
         * ============================================================
         */
        ReadingSubmitResponse response =
                readingSubmissionService.submitTest(
                        savedTest.getId(),
                        request
                );


        /*
         * ============================================================
         * 第七步：验证判分结果
         * ============================================================
         */

        /*
         * 总共两道题。
         */
        assertEquals(
                2,
                response.getTotalQuestions()
        );

        /*
         * 两道题都答对。
         */
        assertEquals(
                2,
                response.getCorrectCount()
        );

        /*
         * 没有错误题目。
         */
        assertEquals(
                0,
                response.getIncorrectCount()
        );

        /*
         * 2 / 2 = 100%
         *
         * 第三个参数 0.001 是 double 比较允许的误差范围。
         */
        assertEquals(
                100.0,
                response.getPercentage(),
                0.001
        );
    }

    /**
     * 测试：
     * <p>
     * 判分时应该忽略：
     * <p>
     * 1. 用户答案前后的空格
     * 2. 用户答案的大小写
     * <p>
     * 例如：
     * <p>
     * 正确答案：
     * TRUE
     * <p>
     * 用户提交：
     * "  true  "
     * <p>
     * 仍然应该判定为正确。
     */
    @Test
    void shouldIgnoreCaseAndWhitespaceWhenComparingAnswers() {

        /*
         * ============================================================
         * 第一步：创建 ReadingTest
         * ============================================================
         */
        ReadingTest readingTest = new ReadingTest();

        readingTest.setTitle("Answer Normalization Test");
        readingTest.setSource("Integration Test");

        ReadingTest savedTest =
                readingTestRepository.save(readingTest);


        /*
         * ============================================================
         * 第二步：创建 ReadingPassage
         * ============================================================
         */
        ReadingPassage passage = new ReadingPassage();

        passage.setReadingTest(savedTest);
        passage.setPassageNumber(1);
        passage.setTitle("Normalization Passage");
        passage.setContent("This is a test passage.");

        ReadingPassage savedPassage =
                readingPassageRepository.save(passage);


        /*
         * ============================================================
         * 第三步：创建一道题
         * ============================================================
         *
         * 数据库中的标准答案是：
         *
         * TRUE
         */
        ReadingQuestion question = new ReadingQuestion();

        question.setReadingPassage(savedPassage);
        question.setQuestionNumber(1);
        question.setQuestionType("TRUE_FALSE_NOT_GIVEN");
        question.setQuestionText("This statement is true.");
        question.setCorrectAnswer("TRUE");

        ReadingQuestion savedQuestion =
                readingQuestionRepository.save(question);


        /*
         * ============================================================
         * 第四步：模拟用户答案
         * ============================================================
         *
         * 故意加入：
         *
         * - 前后空格
         * - 小写字母
         *
         * 用来验证：
         *
         * trim()
         * equalsIgnoreCase()
         */
        Map<Long, String> answers = new HashMap<>();

        answers.put(
                savedQuestion.getId(),
                "  true  "
        );


        /*
         * 创建提交 DTO。
         */
        ReadingSubmitRequest request =
                new ReadingSubmitRequest();

        request.setAnswers(answers);


        /*
         * ============================================================
         * 第五步：调用 ReadingSubmissionService
         * ============================================================
         */
        ReadingSubmitResponse response =
                readingSubmissionService.submitTest(
                        savedTest.getId(),
                        request
                );


        /*
         * ============================================================
         * 第六步：验证判分结果
         * ============================================================
         */

        // 总共只有 1 道题。
        assertEquals(
                1,
                response.getTotalQuestions()
        );

        // 应该答对 1 道。
        assertEquals(
                1,
                response.getCorrectCount()
        );

        // 没有错误题。
        assertEquals(
                0,
                response.getIncorrectCount()
        );

        // 正确率应该是 100%。
        assertEquals(
                100.0,
                response.getPercentage(),
                0.001
        );

        /*
         * 最重要的验证：
         *
         * "  true  "
         *
         * 应该被判定为正确。
         */
        assertTrue(
                response.getQuestions()
                        .get(0)
                        .isCorrect()
        );
    }

    /**
     * 测试：
     * <p>
     * 如果用户提交一个不存在的 Reading Test id，
     * Service 应该直接抛出异常，
     * 而不是继续查询题目或保存 Practice History。
     */
    @Test
    void shouldThrowExceptionWhenReadingTestDoesNotExist() {

        /*
         * ============================================================
         * 第一步：准备一个不存在的 testId
         * ============================================================
         *
         * 这里使用一个很大的 id，
         * 避免和测试数据库里的真实数据冲突。
         */
        Long nonExistingTestId = 999999L;


        /*
         * 即使 answers 是空的也没关系，
         * 因为 Service 应该在处理 answers 之前，
         * 就先检查 Reading Test 是否存在。
         */
        ReadingSubmitRequest request =
                new ReadingSubmitRequest();

        request.setAnswers(new HashMap<>());


        /*
         * ============================================================
         * 第二步：执行并验证异常
         * ============================================================
         */
        ReadingTestNotFoundException exception =
                assertThrows(
                        ReadingTestNotFoundException.class,
                        () -> readingSubmissionService.submitTest(
                                nonExistingTestId,
                                request
                        )
                );


        /*
         * 验证异常信息里包含 Reading test not found。
         *
         * 这样以后如果这段业务规则被意外删除，
         * 测试就会失败。
         */
        assertTrue(
                exception.getMessage()
                        .contains("Reading test not found")
        );
    }

    /**
     * 测试：
     * <p>
     * 如果一个 Reading Test 下面没有任何 Question，
     * 提交时不能发生除以 0。
     * <p>
     * 预期：
     * <p>
     * totalQuestions = 0
     * correctCount = 0
     * incorrectCount = 0
     * percentage = 0.0
     */
    @Test
    void shouldReturnZeroPercentageWhenTestHasNoQuestions() {

        /*
         * 创建一个真实存在的 Reading Test，
         * 但故意不创建 Passage / Question。
         */
        ReadingTest readingTest = new ReadingTest();
        readingTest.setTitle("Empty Reading Test");
        readingTest.setSource("Integration Test");

        ReadingTest savedTest =
                readingTestRepository.save(readingTest);


        /*
         * 用户没有任何答案。
         */
        ReadingSubmitRequest request =
                new ReadingSubmitRequest();

        request.setAnswers(new HashMap<>());


        /*
         * 调用判分 Service。
         */
        ReadingSubmitResponse response =
                readingSubmissionService.submitTest(
                        savedTest.getId(),
                        request
                );


        /*
         * 没有任何题目。
         */
        assertEquals(
                0,
                response.getTotalQuestions()
        );

        assertEquals(
                0,
                response.getCorrectCount()
        );

        assertEquals(
                0,
                response.getIncorrectCount()
        );

        /*
         * 最重要：
         *
         * 不能出现 NaN、Infinity 或异常，
         * 应该明确返回 0%。
         */
        assertEquals(
                0.0,
                response.getPercentage(),
                0.001
        );
    }

    /**
     * 测试：
     *
     * 用户提交 Reading Test 以后，
     * 每一道题的 Review 结果都应该保存成 ReadingPracticeAnswer。
     */
    @Test
    void shouldSavePracticeAnswersAfterSubmit() {

        /*
         * ============================================================
         * 第一步：创建 ReadingTest
         * ============================================================
         */
        ReadingTest readingTest = new ReadingTest();
        readingTest.setTitle("Practice Answer Persistence Test");
        readingTest.setSource("Integration Test");

        ReadingTest savedTest =
                readingTestRepository.save(readingTest);


        /*
         * ============================================================
         * 第二步：创建 Passage
         * ============================================================
         */
        ReadingPassage passage = new ReadingPassage();

        passage.setReadingTest(savedTest);
        passage.setPassageNumber(1);
        passage.setTitle("Practice Answer Passage");
        passage.setContent("This is a test passage.");

        ReadingPassage savedPassage =
                readingPassageRepository.save(passage);


        /*
         * ============================================================
         * 第三步：创建一道题
         * ============================================================
         */
        ReadingQuestion question = new ReadingQuestion();

        question.setReadingPassage(savedPassage);
        question.setQuestionNumber(1);
        question.setQuestionType("TRUE_FALSE_NOT_GIVEN");
        question.setQuestionText("This statement is true.");
        question.setCorrectAnswer("TRUE");

        ReadingQuestion savedQuestion =
                readingQuestionRepository.save(question);


        /*
         * ============================================================
         * 第四步：模拟用户提交答案
         * ============================================================
         */
        Map<Long, String> answers = new HashMap<>();

        answers.put(
                savedQuestion.getId(),
                "FALSE"
        );

        ReadingSubmitRequest request =
                new ReadingSubmitRequest();

        request.setAnswers(answers);


        /*
         * ============================================================
         * 第五步：执行 submit
         * ============================================================
         */
        readingSubmissionService.submitTest(
                savedTest.getId(),
                request
        );


        /*
         * ============================================================
         * 第六步：查询 ReadingPracticeAnswer
         * ============================================================
         *
         * 当前这次测试只创建了一道题，
         * 所以应该只保存一条历史答案。
         */
        var answersInDatabase =
                readingPracticeAnswerRepository.findAll();

        assertEquals(
                1,
                answersInDatabase.size()
        );

        ReadingPracticeAnswer savedAnswer =
                answersInDatabase.get(0);


        /*
         * 验证原始 Question 信息。
         */
        assertEquals(
                savedQuestion.getId(),
                savedAnswer.getQuestionId()
        );

        assertEquals(
                1,
                savedAnswer.getQuestionNumber()
        );


        /*
         * 用户提交的是 FALSE。
         */
        assertEquals(
                "FALSE",
                savedAnswer.getUserAnswer()
        );


        /*
         * 正确答案快照应该是 TRUE。
         */
        assertEquals(
                "TRUE",
                savedAnswer.getCorrectAnswer()
        );


        /*
         * 因为 FALSE != TRUE，
         * 所以应该判定为错误。
         */
        assertEquals(
                false,
                savedAnswer.isCorrect()
        );
    }
}
