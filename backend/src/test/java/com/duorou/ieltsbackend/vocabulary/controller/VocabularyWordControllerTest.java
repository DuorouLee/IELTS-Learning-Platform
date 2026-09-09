package com.duorou.ieltsbackend.vocabulary.controller;

import com.duorou.ieltsbackend.vocabulary.repository.VocabularyWordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * VocabularyWordController 的 REST API 自动化测试。
 *
 * 当前先测试：
 *
 * POST /api/vocabulary/words
 *
 * 目标：
 * 前端发送一个单词以后，
 * Spring Boot 能够把它保存到数据库，
 * 并返回刚刚创建的 VocabularyWord。
 */
@SpringBootTest
@AutoConfigureMockMvc
class VocabularyWordControllerTest {

    /**
     * MockMvc 可以模拟浏览器 / Postman
     * 向 Spring Boot 发送 HTTP 请求。
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试中使用 Repository 清理数据库。
     *
     * 注意：
     * 正式业务代码仍然保持：
     *
     * Controller
     *     ↓
     * Service
     *     ↓
     * Repository
     *
     * 测试代码直接使用 Repository，
     * 只是为了准备和清理测试数据。
     */
    @Autowired
    private VocabularyWordRepository vocabularyWordRepository;

    /**
     * 每个测试开始之前，
     * 清空 vocabulary_word，
     * 防止之前的数据影响当前测试结果。
     */
    @BeforeEach
    void setUp() {
        vocabularyWordRepository.deleteAll();
    }

    /**
     * 测试创建一个 Vocabulary Word。
     *
     * 请求：
     *
     * POST /api/vocabulary/words
     *
     * Body:
     *
     * {
     *   "word": "abandon",
     *   "meaning": "放弃；抛弃",
     *   "exampleSentence": "They had to abandon the plan."
     * }
     *
     * 我们期望：
     *
     * 1. HTTP 状态码为 200
     * 2. 返回的 word 正确
     * 3. 返回的 meaning 正确
     * 4. 返回的 exampleSentence 正确
     * 5. 数据库生成 id
     */
    @Test
    void shouldCreateVocabularyWord() throws Exception {

        String requestBody = """
                {
                  "word": "abandon",
                  "meaning": "放弃；抛弃",
                  "exampleSentence": "They had to abandon the plan."
                }
                """;

        mockMvc.perform(
                        post("/api/vocabulary/words")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.word").value("abandon"))
                .andExpect(jsonPath("$.meaning").value("放弃；抛弃"))
                .andExpect(
                        jsonPath("$.exampleSentence")
                                .value("They had to abandon the plan.")
                );
    }

    /**
     * 测试不能重复创建同一个单词。
     *
     * 场景：
     *
     * 第一次 POST：
     *
     * abandon
     *
     * 应该创建成功。
     *
     * 第二次再次 POST：
     *
     * abandon
     *
     * 应该被后端主动拒绝，
     * 返回 HTTP 400 Bad Request。
     *
     * 这个测试的目的不是依赖数据库 UNIQUE 约束报错，
     * 而是要求我们后面的 Service 层主动判断：
     *
     * vocabularyWordRepository.existsByWord(...)
     */
    @Test
    void shouldRejectDuplicateVocabularyWord() throws Exception {

        String requestBody = """
            {
              "word": "abandon",
              "meaning": "放弃；抛弃",
              "exampleSentence": "They had to abandon the plan."
            }
            """;

        /**
         * 第一次创建。
         *
         * 此时数据库中还没有 abandon，
         * 所以应该成功。
         */
        mockMvc.perform(
                        post("/api/vocabulary/words")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk());

        /**
         * 第二次创建相同的 abandon。
         *
         * 我们希望后端以后返回：
         *
         * HTTP 400 Bad Request
         *
         * 当前代码还没有这个业务校验，
         * 所以这个测试现在应该失败。
         */
        mockMvc.perform(
                        post("/api/vocabulary/words")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }
}
