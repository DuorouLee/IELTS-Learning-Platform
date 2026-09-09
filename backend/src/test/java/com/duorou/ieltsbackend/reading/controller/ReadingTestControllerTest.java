package com.duorou.ieltsbackend.reading.controller;

import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import com.duorou.ieltsbackend.reading.repository.ReadingTestRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingPassageRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingPracticeRecordRepository;
import com.duorou.ieltsbackend.reading.repository.ReadingQuestionRepository;
import com.duorou.ieltsbackend.reading.entity.ReadingPassage;
import com.duorou.ieltsbackend.reading.entity.ReadingQuestion;
import com.duorou.ieltsbackend.reading.entity.ReadingPracticeAnswer;
import com.duorou.ieltsbackend.reading.entity.ReadingPracticeRecord;
import com.duorou.ieltsbackend.reading.repository.ReadingPracticeAnswerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.http.MediaType;

import com.duorou.ieltsbackend.reading.entity.ReadingPracticeRecord;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ReadingTestController 的自动化测试。
 *
 * 这个测试和之前 Repository / Service 测试最大的区别是：
 *
 * 它会模拟真正的 HTTP 请求。
 *
 * 测试链路：
 *
 * MockMvc
 *    ↓
 * ReadingTestController
 *    ↓
 * ReadingTestService
 *    ↓
 * ReadingTestRepository
 *    ↓
 * SQLite
 *
 * 所以它能够帮助我们验证：
 *
 * REST API 到底能不能真正跑通。
 */
@SpringBootTest
@AutoConfigureMockMvc
class ReadingTestControllerTest {


    /**
     * MockMvc 可以理解成：
     *
     * “假的浏览器 / Postman”
     *
     * 它不需要我们真的打开浏览器，
     * 就可以向 Spring Boot Controller
     * 发送 HTTP 请求。
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * 这里直接使用 Repository，
     * 不是因为 Controller 应该访问 Repository。
     *
     * Controller 本身仍然遵循：
     *
     * Controller
     *     ↓
     * Service
     *     ↓
     * Repository
     *
     * 测试里使用 Repository，
     * 只是为了提前准备测试数据。
     */
    @Autowired
    private ReadingTestRepository readingTestRepository;

    /**
     * 用于在 Controller 测试中准备 Reading Passage 测试数据。
     */
    @Autowired
    private ReadingPassageRepository readingPassageRepository;

    /**
     * 用于在 Controller 测试中准备 Reading Question 测试数据。
     */
    @Autowired
    private ReadingQuestionRepository readingQuestionRepository;

    /**
     * 用于验证 Reading Test 提交之后，
     * Practice History 是否真的保存到了数据库。
     */
    @Autowired
    private ReadingPracticeRecordRepository readingPracticeRecordRepository;

    /**
     * Spring Boot 4 默认使用 Jackson 3。
     *
     * JsonMapper 的作用：
     * 把 Java 对象转换成 JSON，
     * 或者把 JSON 转换成 Java 对象。
     *
     * Spring Boot 会自动创建这个 Bean，
     * 所以这里可以直接通过 @Autowired 注入。
     */
    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private ReadingPracticeAnswerRepository readingPracticeAnswerRepository;

    /**
     * 每一个测试运行之前，
     * Spring 都会先执行这个方法。
     *
     * 作用：
     *
     * 清理之前的数据，
     * 避免不同测试互相影响。
     */
    @BeforeEach
    void setUp() {

        /**
         * ReadingPracticeRecord 依赖 ReadingTest。
         *
         * 所以必须先删除 Practice History，
         * 再删除 ReadingTest。
         */
        readingPracticeRecordRepository.deleteAll();

        readingQuestionRepository.deleteAll();
        readingPassageRepository.deleteAll();
        readingTestRepository.deleteAll();
    }

    /**
     * 测试：
     *
     * GET /api/reading/tests
     *
     * 我们希望确认：
     *
     * 1. HTTP 返回 200
     * 2. 返回的是 JSON 数组
     * 3. 数据库里的 ReadingTest
     *    可以通过 REST API 返回
     */
    @Test
    void shouldReturnAllReadingTests() throws Exception {


        // -----------------------------
        // Arrange
        // 准备测试数据
        // -----------------------------

        ReadingTest readingTest = new ReadingTest();

        readingTest.setTitle("Cambridge IELTS Reading Test 1");
        readingTest.setSource("Cambridge IELTS");

        readingTestRepository.save(readingTest);


        // -----------------------------
        // Act + Assert
        //
        // 模拟发送：
        //
        // GET /api/reading/tests
        //
        // 然后检查返回结果
        // -----------------------------

        mockMvc.perform(
                        get("/api/reading/tests")
                )

                // HTTP 应该成功
                .andExpect(
                        status().isOk()
                )

                // JSON 数组第一个对象的 title
                // 应该和数据库里的一样
                .andExpect(
                        jsonPath("$[0].title")
                                .value("Cambridge IELTS Reading Test 1")
                )

                // source 也应该正确
                .andExpect(
                        jsonPath("$[0].source")
                                .value("Cambridge IELTS")
                );
    }

    /**
     * 测试：
     *
     * GET /api/reading/tests/{id}
     *
     * 目标：
     * 根据 id 查询一条 ReadingTest，
     * 并确认 REST API 返回的数据正确。
     */
    @Test
    void shouldReturnReadingTestById() throws Exception {

        // -----------------------------
        // Arrange
        // 先准备一条测试数据
        // -----------------------------
        ReadingTest readingTest = new ReadingTest();

        readingTest.setTitle("Cambridge IELTS Reading Test 2");
        readingTest.setSource("Cambridge IELTS");

        // save() 之后，数据库会给这条数据生成 id
        ReadingTest savedReadingTest =
                readingTestRepository.save(readingTest);


        // -----------------------------
        // Act + Assert
        //
        // 模拟请求：
        //
        // GET /api/reading/tests/{id}
        //
        // savedReadingTest.getId()
        // 就是刚刚数据库生成的真实 id
        // -----------------------------
        mockMvc.perform(
                        get(
                                "/api/reading/tests/{id}",
                                savedReadingTest.getId()
                        )
                )

                // 请求应该成功
                .andExpect(
                        status().isOk()
                )

                // 返回 JSON 的 id
                // 应该和数据库里的 id 一致
                .andExpect(
                        jsonPath("$.id")
                                .value(savedReadingTest.getId())
                )

                // title 应该一致
                .andExpect(
                        jsonPath("$.title")
                                .value("Cambridge IELTS Reading Test 2")
                )

                // source 应该一致
                .andExpect(
                        jsonPath("$.source")
                                .value("Cambridge IELTS")
                );
    }

    /**
     * 测试：
     *
     * POST /api/reading/tests
     *
     * 目标：
     * 1. 发送一个 ReadingTest JSON
     * 2. Controller 接收请求
     * 3. Service 保存数据
     * 4. Repository 写入 SQLite
     * 5. API 返回保存后的 ReadingTest
     */
    @Test
    void shouldCreateReadingTest() throws Exception {

        // -----------------------------
        // Arrange
        // 准备一个要提交的数据对象
        // -----------------------------
        ReadingTest readingTest = new ReadingTest();

        readingTest.setTitle("Cambridge IELTS Reading Test 3");
        readingTest.setSource("Cambridge IELTS");


        // -----------------------------
        // 把 Java 对象转换成 JSON
        // -----------------------------
        String requestJson =
                jsonMapper.writeValueAsString(readingTest);


        // -----------------------------
        // Act + Assert
        //
        // 模拟发送：
        //
        // POST /api/reading/tests
        //
        // 请求体：
        // {
        //   "title": "...",
        //   "source": "..."
        // }
        // -----------------------------
        mockMvc.perform(
                        post("/api/reading/tests")

                                // 告诉服务器：
                                // 我发送的是 JSON
                                .contentType(MediaType.APPLICATION_JSON)

                                // 把 JSON 放进 HTTP 请求体
                                .content(requestJson)
                )

                // 如果你的 Controller 当前返回 200，
                // 这里就用 isOk()
                .andExpect(
                        status().isOk()
                )

                // 返回的数据应该有数据库生成的 id
                .andExpect(
                        jsonPath("$.id").exists()
                )

                // 返回的 title 应该正确
                .andExpect(
                        jsonPath("$.title")
                                .value("Cambridge IELTS Reading Test 3")
                )

                // 返回的 source 应该正确
                .andExpect(
                        jsonPath("$.source")
                                .value("Cambridge IELTS")
                );
    }

    /**
     * 测试：
     *
     * GET /api/reading/tests/{id}/full
     *
     * 目标：
     * 1. Reading Test 的题目可以正常返回
     * 2. correctAnswer 不能在提交之前暴露给前端
     *
     * 这是一个回归测试：
     * 防止以后修改 DTO 时，不小心把正确答案重新放进 /full API。
     */
    @Test
    void fullReadingTestShouldNotExposeCorrectAnswer() throws Exception {

        // -----------------------------
        // Arrange
        // 1. 创建一个 Reading Test
        // -----------------------------
        ReadingTest readingTest = new ReadingTest();
        readingTest.setTitle("Answer Protection Test");
        readingTest.setSource("Controller Test");

        ReadingTest savedTest =
                readingTestRepository.save(readingTest);

        // -----------------------------
        // 2. 创建一篇 Passage
        // -----------------------------
        ReadingPassage passage = new ReadingPassage();

        passage.setReadingTest(savedTest);
        passage.setPassageNumber(1);
        passage.setTitle("Test Passage");
        passage.setContent("This is a test reading passage.");

        ReadingPassage savedPassage =
                readingPassageRepository.save(passage);

        // -----------------------------
        // 3. 创建一道题目
        //
        // 注意：
        // 数据库里确实保存 correctAnswer。
        // 我们就是要验证：
        // 数据库有答案，但 /full API 不返回答案。
        // -----------------------------
        ReadingQuestion question = new ReadingQuestion();

        question.setReadingPassage(savedPassage);
        question.setQuestionNumber(1);
        question.setQuestionType("TRUE_FALSE_NOT_GIVEN");
        question.setQuestionText("This is a test question.");

        // 数据库中保存正确答案
        question.setCorrectAnswer("TRUE");

        readingQuestionRepository.save(question);

        // -----------------------------
        // Act + Assert
        //
        // 请求：
        // GET /api/reading/tests/{id}/full
        // -----------------------------
        mockMvc.perform(
                        get(
                                "/api/reading/tests/{id}/full",
                                savedTest.getId()
                        )
                )

                // API 应该正常返回
                .andExpect(
                        status().isOk()
                )

                // Passage 应该正常返回
                .andExpect(
                        jsonPath("$.passages[0].title")
                                .value("Test Passage")
                )

                // Question 也应该正常返回
                .andExpect(
                        jsonPath("$.passages[0].questions[0].questionText")
                                .value("This is a test question.")
                )

                // 最重要的断言：
                //
                // 虽然数据库里的正确答案是 TRUE，
                // 但是 /full API 中绝对不能出现 correctAnswer。
                .andExpect(
                        jsonPath(
                                "$.passages[0].questions[0].correctAnswer"
                        ).doesNotExist()
                );
    }

    /**
     * 测试：
     *
     * POST /api/reading/tests/{id}/submit
     *
     * 目标：
     * 1. 用户提交正确答案
     * 2. 后端负责判分
     * 3. 提交以后才返回 correctAnswer
     * 4. 返回单题 correct 状态
     * 5. 返回整体正确率
     */
    @Test
    void shouldSubmitReadingTestAndReturnReviewResult() throws Exception {

        // -----------------------------
        // Arrange
        // 1. 创建 Reading Test
        // -----------------------------
        ReadingTest readingTest = new ReadingTest();
        readingTest.setTitle("Reading Submit Test");
        readingTest.setSource("Controller Test");

        ReadingTest savedTest =
                readingTestRepository.save(readingTest);


        // -----------------------------
        // 2. 创建 Passage
        // -----------------------------
        ReadingPassage passage = new ReadingPassage();

        passage.setReadingTest(savedTest);
        passage.setPassageNumber(1);
        passage.setTitle("Submit Test Passage");
        passage.setContent("This is a test passage.");

        ReadingPassage savedPassage =
                readingPassageRepository.save(passage);


        // -----------------------------
        // 3. 创建一道 Question
        //
        // 正确答案设置为 TRUE。
        // -----------------------------
        ReadingQuestion question = new ReadingQuestion();

        question.setReadingPassage(savedPassage);
        question.setQuestionNumber(1);
        question.setQuestionType("TRUE_FALSE_NOT_GIVEN");
        question.setQuestionText("This is a test question.");
        question.setCorrectAnswer("TRUE");

        ReadingQuestion savedQuestion =
                readingQuestionRepository.save(question);


        // -----------------------------
        // 4. 模拟前端提交答案
        //
        // answers 的 key 必须是数据库中的 questionId。
        //
        // 最终 JSON 类似：
        //
        // {
        //   "answers": {
        //     "15": "TRUE"
        //   }
        // }
        // -----------------------------
        String requestJson = """
            {
              "answers": {
                "%d": "TRUE"
              }
            }
            """.formatted(savedQuestion.getId());


        // -----------------------------
        // Act + Assert
        //
        // 模拟发送：
        //
        // POST /api/reading/tests/{id}/submit
        // -----------------------------
        mockMvc.perform(
                        post(
                                "/api/reading/tests/{id}/submit",
                                savedTest.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )

                // HTTP 请求应该成功
                .andExpect(
                        status().isOk()
                )

                // 一共只有 1 道题
                .andExpect(
                        jsonPath("$.totalQuestions")
                                .value(1)
                )

                // 用户答对了这 1 道题
                .andExpect(
                        jsonPath("$.correctCount")
                                .value(1)
                )

                // 没有错误题
                .andExpect(
                        jsonPath("$.incorrectCount")
                                .value(0)
                )

                // 1 / 1 = 100%
                .andExpect(
                        jsonPath("$.percentage")
                                .value(100.0)
                )

                // -----------------------------
                // 以下是最关键的 Review 验证
                // -----------------------------

                // 返回的必须是刚才那道题
                .andExpect(
                        jsonPath("$.questions[0].questionId")
                                .value(savedQuestion.getId())
                )

                // 用户提交的答案
                .andExpect(
                        jsonPath("$.questions[0].userAnswer")
                                .value("TRUE")
                )

                // submit 以后允许返回正确答案
                .andExpect(
                        jsonPath("$.questions[0].correctAnswer")
                                .value("TRUE")
                )

                // 后端判定该题正确
                .andExpect(
                        jsonPath("$.questions[0].correct")
                                .value(true)
                );
    }

    /**
     * 测试：
     *
     * POST /api/reading/tests/{id}/submit
     *
     * 用户提交错误答案时：
     * 1. correctCount 应该是 0
     * 2. percentage 应该是 0
     * 3. 单题 correct 应该是 false
     * 4. submit 后仍然要返回正确答案，供 Review 使用
     */
    @Test
    void shouldReturnIncorrectReviewWhenAnswerIsWrong() throws Exception {

        // -----------------------------
        // Arrange
        // 1. 创建 Reading Test
        // -----------------------------
        ReadingTest readingTest = new ReadingTest();
        readingTest.setTitle("Wrong Answer Test");
        readingTest.setSource("Controller Test");

        ReadingTest savedTest =
                readingTestRepository.save(readingTest);


        // -----------------------------
        // 2. 创建 Passage
        // -----------------------------
        ReadingPassage passage = new ReadingPassage();

        passage.setReadingTest(savedTest);
        passage.setPassageNumber(1);
        passage.setTitle("Wrong Answer Passage");
        passage.setContent("This is a test passage.");

        ReadingPassage savedPassage =
                readingPassageRepository.save(passage);


        // -----------------------------
        // 3. 创建一道题
        //
        // 正确答案是 TRUE
        // -----------------------------
        ReadingQuestion question = new ReadingQuestion();

        question.setReadingPassage(savedPassage);
        question.setQuestionNumber(1);
        question.setQuestionType("TRUE_FALSE_NOT_GIVEN");
        question.setQuestionText("This statement is true.");
        question.setCorrectAnswer("TRUE");

        ReadingQuestion savedQuestion =
                readingQuestionRepository.save(question);


        // -----------------------------
        // 4. 故意提交错误答案 FALSE
        // -----------------------------
        String requestJson = """
            {
              "answers": {
                "%d": "FALSE"
              }
            }
            """.formatted(savedQuestion.getId());


        // -----------------------------
        // Act + Assert
        // -----------------------------
        mockMvc.perform(
                        post(
                                "/api/reading/tests/{id}/submit",
                                savedTest.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )

                .andExpect(status().isOk())

                // 一共 1 道题
                .andExpect(
                        jsonPath("$.totalQuestions")
                                .value(1)
                )

                // 没有答对
                .andExpect(
                        jsonPath("$.correctCount")
                                .value(0)
                )

                // 1 道答错
                .andExpect(
                        jsonPath("$.incorrectCount")
                                .value(1)
                )

                // 正确率 0%
                .andExpect(
                        jsonPath("$.percentage")
                                .value(0.0)
                )

                // 用户实际提交 FALSE
                .andExpect(
                        jsonPath("$.questions[0].userAnswer")
                                .value("FALSE")
                )

                // 正确答案仍然是 TRUE
                .andExpect(
                        jsonPath("$.questions[0].correctAnswer")
                                .value("TRUE")
                )

                // 后端必须判断为错误
                .andExpect(
                        jsonPath("$.questions[0].correct")
                                .value(false)
                );
    }

    /**
     * 测试：
     *
     * 用户没有回答某一道题时，
     * 后端应该把它判定为错误，
     * 并且 Review 中的 userAnswer 应该为空。
     */
    @Test
    void shouldTreatUnansweredQuestionAsIncorrect() throws Exception {

        // -----------------------------
        // Arrange
        // 1. 创建 Reading Test
        // -----------------------------
        ReadingTest readingTest = new ReadingTest();
        readingTest.setTitle("Unanswered Test");
        readingTest.setSource("Controller Test");

        ReadingTest savedTest =
                readingTestRepository.save(readingTest);


        // -----------------------------
        // 2. 创建 Passage
        // -----------------------------
        ReadingPassage passage = new ReadingPassage();

        passage.setReadingTest(savedTest);
        passage.setPassageNumber(1);
        passage.setTitle("Unanswered Passage");
        passage.setContent("This is a test passage.");

        ReadingPassage savedPassage =
                readingPassageRepository.save(passage);


        // -----------------------------
        // 3. 创建一道题
        // -----------------------------
        ReadingQuestion question = new ReadingQuestion();

        question.setReadingPassage(savedPassage);
        question.setQuestionNumber(1);
        question.setQuestionType("TRUE_FALSE_NOT_GIVEN");
        question.setQuestionText("This is a test question.");
        question.setCorrectAnswer("TRUE");

        ReadingQuestion savedQuestion =
                readingQuestionRepository.save(question);


        // -----------------------------
        // 4. answers 是空对象
        //
        // 表示用户没有回答任何题目。
        // -----------------------------
        String requestJson = """
            {
              "answers": {}
            }
            """;


        // -----------------------------
        // Act + Assert
        // -----------------------------
        mockMvc.perform(
                        post(
                                "/api/reading/tests/{id}/submit",
                                savedTest.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )

                .andExpect(status().isOk())

                // 一共 1 道题
                .andExpect(
                        jsonPath("$.totalQuestions")
                                .value(1)
                )

                // 未作答，所以答对数量为 0
                .andExpect(
                        jsonPath("$.correctCount")
                                .value(0)
                )

                // 未作答也算错误
                .andExpect(
                        jsonPath("$.incorrectCount")
                                .value(1)
                )

                // 正确率应该是 0%
                .andExpect(
                        jsonPath("$.percentage")
                                .value(0.0)
                )

                // 返回的仍然应该是刚才那道题
                .andExpect(
                        jsonPath("$.questions[0].questionId")
                                .value(savedQuestion.getId())
                )

                // 用户没有回答，所以 userAnswer 不应该有值
                .andExpect(
                        jsonPath("$.questions[0].userAnswer")
                                .doesNotExist()
                )

                // submit 后允许查看正确答案
                .andExpect(
                        jsonPath("$.questions[0].correctAnswer")
                                .value("TRUE")
                )

                // 未作答应该判错
                .andExpect(
                        jsonPath("$.questions[0].correct")
                                .value(false)
                );
    }

    /**
     * 测试：
     *
     * POST /api/reading/tests/{id}/submit
     *
     * 用户提交答案以后，
     * 后端除了返回判分结果，
     * 还应该保存一条 Reading Practice History。
     */
    @Test
    void shouldSavePracticeHistoryAfterSubmit() throws Exception {

        // -----------------------------
        // Arrange
        // 1. 创建 Reading Test
        // -----------------------------
        ReadingTest readingTest = new ReadingTest();
        readingTest.setTitle("Practice History Test");
        readingTest.setSource("Controller Test");

        ReadingTest savedTest =
                readingTestRepository.save(readingTest);


        // -----------------------------
        // 2. 创建 Passage
        // -----------------------------
        ReadingPassage passage = new ReadingPassage();

        passage.setReadingTest(savedTest);
        passage.setPassageNumber(1);
        passage.setTitle("History Passage");
        passage.setContent("This is a test passage.");

        ReadingPassage savedPassage =
                readingPassageRepository.save(passage);


        // -----------------------------
        // 3. 创建一道题
        // -----------------------------
        ReadingQuestion question = new ReadingQuestion();

        question.setReadingPassage(savedPassage);
        question.setQuestionNumber(1);
        question.setQuestionType("TRUE_FALSE_NOT_GIVEN");
        question.setQuestionText("This is a test question.");
        question.setCorrectAnswer("TRUE");

        ReadingQuestion savedQuestion =
                readingQuestionRepository.save(question);


        // -----------------------------
        // 4. 提交正确答案
        // -----------------------------
        String requestJson = """
            {
              "answers": {
                "%d": "TRUE"
              }
            }
            """.formatted(savedQuestion.getId());


        // -----------------------------
        // Act
        // 调用 submit API
        // -----------------------------
        mockMvc.perform(
                        post(
                                "/api/reading/tests/{id}/submit",
                                savedTest.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk());


        // -----------------------------
        // Assert
        // 读取 Practice History
        // -----------------------------
        var records =
                readingPracticeRecordRepository.findAllByOrderBySubmittedAtDesc();

        // 应该保存 1 条记录
        assertEquals(1, records.size());

        var record = records.get(0);

        // Practice History 应该关联到刚才提交的 Reading Test。
        // Hibernate 对关联对象的 ID 通常可以直接从代理对象中读取，
        // 不需要额外加载整个 ReadingTest。
        assertEquals(
                savedTest.getId(),
                record.getReadingTest().getId()
        );

        // 验证本次练习成绩
        assertEquals(1, record.getCorrectCount());
        assertEquals(1, record.getTotalQuestions());

        // 验证正确率
        assertEquals(100.0, record.getPercentage());

        // 验证提交时间已经生成
        assertNotNull(record.getSubmittedAt());
    }

    /**
     * 测试：
     *
     * DELETE /api/reading/practice-history/{id}
     *
     * 删除一条 Reading Practice History 后，
     * 数据库中应该不再存在这条记录。
     */
    @Test
    void shouldDeleteReadingPracticeHistory() throws Exception {

        // -----------------------------
        // Arrange
        // 1. 创建一个 Reading Test
        // -----------------------------
        ReadingTest readingTest = new ReadingTest();
        readingTest.setTitle("Delete History Test");
        readingTest.setSource("Controller Test");

        ReadingTest savedTest =
                readingTestRepository.save(readingTest);


        // -----------------------------
        // 2. 创建一条 Practice History
        // -----------------------------
        ReadingPracticeRecord record = new ReadingPracticeRecord();

        record.setReadingTest(savedTest);
        record.setCorrectCount(1);
        record.setTotalQuestions(1);
        record.setPercentage(100.0);
        record.setSubmittedAt(System.currentTimeMillis());

        ReadingPracticeRecord savedRecord =
                readingPracticeRecordRepository.save(record);


        // 删除之前，数据库中应该存在这条记录
        assertTrue(
                readingPracticeRecordRepository.existsById(
                        savedRecord.getId()
                )
        );


        // -----------------------------
        // Act
        // 调用 DELETE API
        // -----------------------------
        mockMvc.perform(
                        delete(
                                "/api/reading/practice-history/{id}",
                                savedRecord.getId()
                        )
                )
                .andExpect(status().isOk());


        // -----------------------------
        // Assert
        // 删除以后数据库中不应该再存在
        // -----------------------------
        assertFalse(
                readingPracticeRecordRepository.existsById(
                        savedRecord.getId()
                )
        );
    }

    /**
     * 测试：
     *
     * GET /api/reading/practice-history
     *
     * 目标：
     * Practice History 应该按照 submittedAt
     * 从新到旧返回。
     */
    @Test
    void shouldReturnPracticeHistoryFromNewestToOldest() throws Exception {

        // -----------------------------
        // Arrange
        // 1. 创建两个 Reading Test
        // -----------------------------
        ReadingTest firstTest = new ReadingTest();
        firstTest.setTitle("Older Practice Test");
        firstTest.setSource("Controller Test");

        ReadingTest savedFirstTest =
                readingTestRepository.save(firstTest);

        ReadingTest secondTest = new ReadingTest();
        secondTest.setTitle("Newer Practice Test");
        secondTest.setSource("Controller Test");

        ReadingTest savedSecondTest =
                readingTestRepository.save(secondTest);


        // -----------------------------
        // 2. 创建较早的一条历史记录
        // -----------------------------
        ReadingPracticeRecord olderRecord =
                new ReadingPracticeRecord();

        olderRecord.setReadingTest(savedFirstTest);
        olderRecord.setCorrectCount(1);
        olderRecord.setTotalQuestions(2);
        olderRecord.setPercentage(50.0);

        // 较早时间
        olderRecord.setSubmittedAt(1000L);

        readingPracticeRecordRepository.save(olderRecord);


        // -----------------------------
        // 3. 创建较新的一条历史记录
        // -----------------------------
        ReadingPracticeRecord newerRecord =
                new ReadingPracticeRecord();

        newerRecord.setReadingTest(savedSecondTest);
        newerRecord.setCorrectCount(2);
        newerRecord.setTotalQuestions(2);
        newerRecord.setPercentage(100.0);

        // 较新时间
        newerRecord.setSubmittedAt(2000L);

        readingPracticeRecordRepository.save(newerRecord);


        // -----------------------------
        // Act + Assert
        // -----------------------------
        mockMvc.perform(
                        get("/api/reading/practice-history")
                )
                .andExpect(status().isOk())

                // 第一条必须是较新的记录
                .andExpect(
                        jsonPath("$[0].submittedAt")
                                .value(2000)
                )

                .andExpect(
                        jsonPath("$[0].percentage")
                                .value(100.0)
                )

                // 第二条才是较旧记录
                .andExpect(
                        jsonPath("$[1].submittedAt")
                                .value(1000)
                )

                .andExpect(
                        jsonPath("$[1].percentage")
                                .value(50.0)
                );
    }

    /**
     * 测试：
     *
     * POST /api/reading/tests/{id}/submit
     *
     * 当 Reading Test 不存在时，
     * 应该返回 HTTP 404，
     * 而不是 500 Internal Server Error。
     */
    @Test
    void shouldReturn404WhenSubmittingNonExistingReadingTest() throws Exception {

        /*
         * 准备一个基本不存在的 testId。
         */
        long nonExistingTestId = 999999L;

        /*
         * 即使答案为空也没关系，
         * 因为 Service 应该先检查 Reading Test 是否存在。
         */
        String requestJson = """
            {
              "answers": {}
            }
            """;

        mockMvc.perform(
                        post(
                                "/api/reading/tests/{id}/submit",
                                nonExistingTestId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )

                // GlobalExceptionHandler
                // 应该把 IllegalArgumentException 转成 404。
                .andExpect(
                        status().isNotFound()
                )

                // 同时验证返回给前端的错误信息。
                .andExpect(
                        jsonPath("$.message")
                                .value("Reading test not found: 999999")
                );
    }

    /**
     * 测试：
     *
     * GET /api/reading/tests/{id}
     *
     * 当 Reading Test 不存在时，
     * 应该返回 HTTP 404，
     * 并返回统一的错误信息。
     */
    @Test
    void shouldReturn404WhenReadingTestDoesNotExist() throws Exception {

        /*
         * 使用一个基本不可能存在的 Test ID。
         */
        long nonExistingTestId = 999999L;

        mockMvc.perform(
                        get(
                                "/api/reading/tests/{id}",
                                nonExistingTestId
                        )
                )

                // ReadingTestNotFoundException
                // 应该被 GlobalExceptionHandler 转换成 404。
                .andExpect(
                        status().isNotFound()
                )

                // 同时验证错误 JSON。
                .andExpect(
                        jsonPath("$.message")
                                .value("Reading test not found: 999999")
                );
    }

    /**
     * 测试：
     *
     * GET /api/reading/tests/{id}/full
     *
     * 当 Reading Test 不存在时，
     * 应该统一返回 HTTP 404。
     */
    @Test
    void shouldReturn404WhenFullReadingTestDoesNotExist() throws Exception {

        long nonExistingTestId = 999999L;

        mockMvc.perform(
                        get(
                                "/api/reading/tests/{id}/full",
                                nonExistingTestId
                        )
                )

                // ReadingTestNotFoundException
                // 应该由 GlobalExceptionHandler 转换成 404。
                .andExpect(
                        status().isNotFound()
                )

                // 返回统一错误信息。
                .andExpect(
                        jsonPath("$.message")
                                .value("Reading test not found: 999999")
                );
    }

    /**
     * 测试：
     *
     * GET /api/reading/practice-history/{id}
     *
     * 应该返回：
     * - Practice 基本信息
     * - 成绩
     * - 每一道题的历史答案
     */
    @Test
    void shouldReturnReadingPracticeHistoryDetail() throws Exception {

        // -----------------------------
        // Arrange
        // 1. 创建 Reading Test
        // -----------------------------
        ReadingTest readingTest = new ReadingTest();
        readingTest.setTitle("History Detail Test");
        readingTest.setSource("Controller Test");

        ReadingTest savedTest =
                readingTestRepository.save(readingTest);


        // -----------------------------
        // 2. 创建 Practice Record
        // -----------------------------
        ReadingPracticeRecord record =
                new ReadingPracticeRecord();

        record.setReadingTest(savedTest);
        record.setCorrectCount(1);
        record.setTotalQuestions(2);
        record.setPercentage(50.0);
        record.setSubmittedAt(2000L);

        ReadingPracticeRecord savedRecord =
                readingPracticeRecordRepository.save(record);


        // -----------------------------
        // 3. 创建第一道历史答案
        // -----------------------------
        ReadingPracticeAnswer firstAnswer =
                new ReadingPracticeAnswer();

        firstAnswer.setPracticeRecord(savedRecord);
        firstAnswer.setQuestionId(101L);
        firstAnswer.setQuestionNumber(1);
        firstAnswer.setUserAnswer("FALSE");
        firstAnswer.setCorrectAnswer("TRUE");
        firstAnswer.setCorrect(false);

        readingPracticeAnswerRepository.save(firstAnswer);


        // -----------------------------
        // 4. 创建第二道历史答案
        // -----------------------------
        ReadingPracticeAnswer secondAnswer =
                new ReadingPracticeAnswer();

        secondAnswer.setPracticeRecord(savedRecord);
        secondAnswer.setQuestionId(102L);
        secondAnswer.setQuestionNumber(2);
        secondAnswer.setUserAnswer("D");
        secondAnswer.setCorrectAnswer("D");
        secondAnswer.setCorrect(true);

        readingPracticeAnswerRepository.save(secondAnswer);


        // -----------------------------
        // Act + Assert
        // -----------------------------
        mockMvc.perform(
                        get(
                                "/api/reading/practice-history/{id}",
                                savedRecord.getId()
                        )
                )
                .andExpect(status().isOk())

                // Practice 基本信息
                .andExpect(
                        jsonPath("$.id")
                                .value(savedRecord.getId())
                )

                .andExpect(
                        jsonPath("$.testId")
                                .value(savedTest.getId())
                )

                .andExpect(
                        jsonPath("$.testTitle")
                                .value("History Detail Test")
                )

                .andExpect(
                        jsonPath("$.correctCount")
                                .value(1)
                )

                .andExpect(
                        jsonPath("$.totalQuestions")
                                .value(2)
                )

                .andExpect(
                        jsonPath("$.percentage")
                                .value(50.0)
                )

                // 第一题
                .andExpect(
                        jsonPath("$.answers[0].questionNumber")
                                .value(1)
                )

                .andExpect(
                        jsonPath("$.answers[0].userAnswer")
                                .value("FALSE")
                )

                .andExpect(
                        jsonPath("$.answers[0].correctAnswer")
                                .value("TRUE")
                )

                .andExpect(
                        jsonPath("$.answers[0].correct")
                                .value(false)
                )

                // 第二题
                .andExpect(
                        jsonPath("$.answers[1].questionNumber")
                                .value(2)
                )

                .andExpect(
                        jsonPath("$.answers[1].userAnswer")
                                .value("D")
                )

                .andExpect(
                        jsonPath("$.answers[1].correctAnswer")
                                .value("D")
                )

                .andExpect(
                        jsonPath("$.answers[1].correct")
                                .value(true)
                );
    }
}
