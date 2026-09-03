package com.duorou.ieltsbackend.reading.controller;

import com.duorou.ieltsbackend.reading.entity.ReadingQuestion;
import com.duorou.ieltsbackend.reading.service.ReadingQuestionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ReadingQuestionController
 * <p>
 * 作用：
 * 接收和 ReadingQuestion 相关的 HTTP 请求。
 * <p>
 * 调用链路：
 * <p>
 * HTTP Request
 * ↓
 * ReadingQuestionController
 * ↓
 * ReadingQuestionService
 * ↓
 * ReadingQuestionRepository
 * ↓
 * SQLite
 */
@RestController
@RequestMapping("/api/reading")
public class ReadingQuestionController {

    /**
     * Controller 不直接操作数据库。
     * <p>
     * 它通过 Service 来处理业务逻辑。
     */
    private final ReadingQuestionService readingQuestionService;

    /**
     * 构造器注入。
     * <p>
     * Spring 会自动找到 ReadingQuestionService，
     * 并传入这里。
     */
    public ReadingQuestionController(
            ReadingQuestionService readingQuestionService
    ) {
        this.readingQuestionService = readingQuestionService;
    }

    /**
     * 查询所有 Question。
     * <p>
     * GET /api/reading/questions
     */
    @GetMapping
    public List<ReadingQuestion> getAllQuestions() {
        return readingQuestionService.findAll();
    }

    /**
     * 根据 id 查询 Question。
     * <p>
     * 例如：
     * GET /api/reading/questions/1
     */
    @GetMapping("/{id}")
    public ReadingQuestion getQuestionById(
            @PathVariable Long id
    ) {
        return readingQuestionService.findById(id);
    }

    /**
     * 创建一个新的 Question。
     * <p>
     * POST /api/reading/questions
     *
     * @RequestBody： 把前端发来的 JSON
     * 转换成 ReadingQuestion Java 对象。
     */
    @PostMapping
    public ReadingQuestion createQuestion(
            @RequestBody ReadingQuestion readingQuestion
    ) {
        return readingQuestionService.create(readingQuestion);
    }

    /**
     * 查询某个 Passage 下的全部 Question。
     *
     * 例如：
     * GET /api/reading/passages/3/questions
     */
    @GetMapping("/passages/{passageId}/questions")
    public List<ReadingQuestion> getQuestionsByPassageId(
            @PathVariable Long passageId
    ) {
        return readingQuestionService.findByPassageId(passageId);
    }
}
