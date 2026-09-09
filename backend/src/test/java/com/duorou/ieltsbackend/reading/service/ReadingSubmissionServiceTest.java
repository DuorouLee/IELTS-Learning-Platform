package com.duorou.ieltsbackend.reading.service;

import com.duorou.ieltsbackend.reading.dto.ReadingSubmitRequest;
import com.duorou.ieltsbackend.reading.dto.ReadingSubmitResponse;
import com.duorou.ieltsbackend.reading.entity.ReadingPassage;
import com.duorou.ieltsbackend.reading.entity.ReadingQuestion;
import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import com.duorou.ieltsbackend.reading.repository.ReadingPassageRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingQuestionRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingTestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ReadingSubmissionService 集成测试。
 *
 * 这个测试会真实走：
 *
 * ReadingSubmissionService
 * ↓
 * ReadingTestRepository
 * ↓
 * ReadingQuestionRepository
 * ↓
 * Hibernate / JPA
 * ↓
 * SQLite
 *
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
     *
     * 注意：
     * 测试的重点仍然是 ReadingSubmissionService，
     * Repository 只是帮助我们建立：
     *
     * ReadingTest
     * ↓
     * ReadingPassage
     * ↓
     * ReadingQuestion
     *
     * 这一套真实数据库数据。
     */
    @Autowired
    private ReadingTestRepository readingTestRepository;

    @Autowired
    private ReadingPassageRepository readingPassageRepository;

    @Autowired
    private ReadingQuestionRepository readingQuestionRepository;

    /**
     * 测试场景：
     *
     * 当前 Test 有两道题：
     *
     * Question 1 正确答案：viii
     * Question 2 正确答案：D
     *
     * 用户两道题全部答对。
     *
     * 预期结果：
     *
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
     *
     * 判分时应该忽略：
     *
     * 1. 用户答案前后的空格
     * 2. 用户答案的大小写
     *
     * 例如：
     *
     * 正确答案：
     * TRUE
     *
     * 用户提交：
     * "  true  "
     *
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
}
