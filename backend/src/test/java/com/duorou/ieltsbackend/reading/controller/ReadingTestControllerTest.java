package com.duorou.ieltsbackend.reading.controller;

import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import com.duorou.ieltsbackend.reading.repository.ReadingTestRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.http.MediaType;

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
}
