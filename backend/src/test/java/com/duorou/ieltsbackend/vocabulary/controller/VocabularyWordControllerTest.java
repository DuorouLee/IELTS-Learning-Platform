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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import com.duorou.ieltsbackend.vocabulary.entity.VocabularyWord;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    /**
     * 测试根据 id 查询一个 Vocabulary Word。
     *
     * 测试步骤：
     *
     * 1. 先往数据库保存一个单词
     * 2. 获取它生成的 id
     * 3. 请求 GET /api/vocabulary/words/{id}
     * 4. 验证返回的数据正确
     */
    @Test
    void shouldGetVocabularyWordById() throws Exception {

        VocabularyWord word = new VocabularyWord();
        word.setWord("allocate");
        word.setMeaning("分配");
        word.setExampleSentence("The government allocated more money to education.");

        VocabularyWord savedWord = vocabularyWordRepository.save(word);

        mockMvc.perform(
                        get("/api/vocabulary/words/{id}", savedWord.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedWord.getId()))
                .andExpect(jsonPath("$.word").value("allocate"))
                .andExpect(jsonPath("$.meaning").value("分配"))
                .andExpect(
                        jsonPath("$.exampleSentence")
                                .value("The government allocated more money to education.")
                );
    }

    /**
     * 测试查询不存在的 Vocabulary Word。
     *
     * 期望：
     *
     * GET /api/vocabulary/words/999999
     *
     * 返回 HTTP 404。
     */
    @Test
    void shouldReturn404WhenVocabularyWordDoesNotExist() throws Exception {

        mockMvc.perform(
                        get("/api/vocabulary/words/{id}", 999999L)
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.message")
                                .value("Vocabulary word not found: 999999")
                );
    }

    /**
     * 测试根据 id 删除一个 Vocabulary Word。
     *
     * 测试步骤：
     *
     * 1. 先保存一个单词
     * 2. 调用 DELETE /api/vocabulary/words/{id}
     * 3. 期望返回 204 No Content
     * 4. 再检查数据库，确认这个单词已经不存在
     */
    @Test
    void shouldDeleteVocabularyWordById() throws Exception {

        VocabularyWord word = new VocabularyWord();
        word.setWord("derive");
        word.setMeaning("获得；得出");
        word.setExampleSentence("Many English words derive from Latin.");

        VocabularyWord savedWord = vocabularyWordRepository.save(word);

        mockMvc.perform(
                        delete("/api/vocabulary/words/{id}", savedWord.getId())
                )
                .andExpect(status().isNoContent());

        /**
         * 删除以后，
         * Repository 不应该再找到这条数据。
         */
        boolean exists = vocabularyWordRepository.existsById(
                savedWord.getId()
        );

        assertFalse(exists);
    }
}
