package com.duorou.ieltsbackend.vocabulary.controller;

import com.duorou.ieltsbackend.vocabulary.entity.VocabularyWord;
import com.duorou.ieltsbackend.vocabulary.service.VocabularyWordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * VocabularyWordController
 *
 * Vocabulary 模块的 HTTP 接口入口。
 *
 * 当前调用链：
 *
 * Browser / Vue
 *      ↓
 * Controller
 *      ↓
 * Service
 *      ↓
 * Repository
 *      ↓
 * Database
 *
 * 当前第一步只提供：
 *
 * GET /api/vocabulary/words
 *
 * 用来查询数据库中的全部单词。
 */
@RestController
@RequestMapping("/api/vocabulary/words")
public class VocabularyWordController {

    /**
     * Controller 不直接访问 Repository。
     *
     * Controller 只调用 Service，
     * 保持项目统一的分层结构。
     */
    private final VocabularyWordService vocabularyWordService;

    /**
     * 构造器注入 VocabularyWordService。
     *
     * Spring 创建 Controller 时，
     * 会自动把 VocabularyWordService 注入进来。
     */
    public VocabularyWordController(
            VocabularyWordService vocabularyWordService
    ) {
        this.vocabularyWordService = vocabularyWordService;
    }

    /**
     * 查询所有单词。
     *
     * 请求：
     *
     * GET /api/vocabulary/words
     *
     * 返回：
     *
     * [
     *   {
     *     "id": 1,
     *     "word": "abandon",
     *     "meaning": "放弃；抛弃",
     *     "exampleSentence": "...",
     *     "createdAt": "..."
     *   }
     * ]
     *
     * 如果数据库目前没有单词，
     * 返回的就是空数组：
     *
     * []
     */
    @GetMapping
    public List<VocabularyWord> getAllWords() {
        return vocabularyWordService.getAllWords();
    }

    /**
     * 创建一个新的单词。
     *
     * 请求：
     *
     * POST /api/vocabulary/words
     *
     * Body 示例：
     *
     * {
     *   "word": "abandon",
     *   "meaning": "放弃；抛弃",
     *   "exampleSentence": "They had to abandon the plan."
     * }
     *
     * @RequestBody
     * 会把前端发送的 JSON
     * 自动转换成 VocabularyWord Java 对象。
     */
    @PostMapping
    public VocabularyWord createWord(
            @RequestBody VocabularyWord vocabularyWord
    ) {
        return vocabularyWordService.createWord(vocabularyWord);
    }

    /**
     * 根据 id 查询一个 Vocabulary Word。
     *
     * 例如：
     *
     * GET /api/vocabulary/words/3
     *
     * @PathVariable 会把 URL 中的 3
     * 传给方法参数 id。
     */
    @GetMapping("/{id}")
    public VocabularyWord getWordById(
            @PathVariable Long id
    ) {
        return vocabularyWordService.getWordById(id);
    }
}
