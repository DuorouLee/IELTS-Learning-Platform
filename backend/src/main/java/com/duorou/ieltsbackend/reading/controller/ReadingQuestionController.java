package com.duorou.ieltsbackend.reading.controller;

import com.duorou.ieltsbackend.reading.entity.ReadingQuestion;
import com.duorou.ieltsbackend.reading.service.ReadingQuestionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ReadingQuestionController
 *
 * 作用：
 * 接收和 ReadingQuestion 相关的 HTTP 请求。
 *
 * 调用链路：
 *
 * HTTP Request
 *      ↓
 * ReadingQuestionController
 *      ↓
 * ReadingQuestionService
 *      ↓
 * ReadingQuestionRepository
 *      ↓
 * SQLite
 */
@RestController
@RequestMapping("/api/reading/questions")
public class ReadingQuestionController {

    /**
     * Controller 不直接操作数据库。
     *
     * 它通过 Service 来处理业务逻辑。
     */
    private final ReadingQuestionService readingQuestionService;

    /**
     * 构造器注入。
     *
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
     *
     * GET /api/reading/questions
     */
    @GetMapping
    public List<ReadingQuestion> getAllQuestions() {
        return readingQuestionService.findAll();
    }

    /**
     * 根据 id 查询 Question。
     *
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
     *
     * POST /api/reading/questions
     *
     * @RequestBody：
     * 把前端发来的 JSON
     * 转换成 ReadingQuestion Java 对象。
     */
    @PostMapping
    public ReadingQuestion createQuestion(
            @RequestBody ReadingQuestion readingQuestion
    ) {
        return readingQuestionService.create(readingQuestion);
    }
}
