package com.duorou.ieltsbackend.reading.controller;

import com.duorou.ieltsbackend.reading.dto.ReadingTestDetailResponse;
import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import com.duorou.ieltsbackend.reading.service.ReadingTestService;
import org.springframework.web.bind.annotation.*;
import com.duorou.ieltsbackend.reading.dto.ReadingSubmitRequest;
import com.duorou.ieltsbackend.reading.dto.ReadingSubmitResponse;
import com.duorou.ieltsbackend.reading.service.ReadingSubmissionService;

import java.util.List;

/**
 * ReadingTestController
 * <p>
 * 作用：
 * 对外提供 Reading Test 相关的 HTTP API。
 * <p>
 * 整体调用关系：
 * <p>
 * Browser / Postman / Frontend
 * ↓
 * ReadingTestController
 * ↓
 * ReadingTestService
 * ↓
 * ReadingTestRepository
 * ↓
 * SQLite
 */
@RestController
@RequestMapping("/api/reading/tests")
public class ReadingTestController {

    /**
     * Controller 不直接访问 Repository。
     * <p>
     * Controller 只调用 Service，
     * 这样可以保持项目分层清晰。
     */
    private final ReadingTestService readingTestService;

    /**
     * ReadingSubmissionService
     *
     * 专门负责：
     *
     * 用户提交答案
     * ↓
     * 后端判分
     * ↓
     * 返回成绩
     */
    private final ReadingSubmissionService readingSubmissionService;

    /**
     * 构造器注入。
     *
     * Spring 会自动注入：
     *
     * ReadingTestService
     * ReadingSubmissionService
     */
    public ReadingTestController(
            ReadingTestService readingTestService,
            ReadingSubmissionService readingSubmissionService
    ) {
        this.readingTestService = readingTestService;
        this.readingSubmissionService = readingSubmissionService;
    }

    /**
     * 查询所有 Reading Test。
     * <p>
     * HTTP:
     * GET /api/reading/tests
     * <p>
     * 例如浏览器访问：
     * http://localhost:8080/api/reading/tests
     */
    @GetMapping
    public List<ReadingTest> findAll() {
        return readingTestService.findAll();
    }

    /**
     * 根据 id 查询一个 Reading Test。
     * <p>
     * HTTP:
     * GET /api/reading/tests/1
     *
     * @PathVariable 表示把 URL 中的数字读取出来。
     * <p>
     * 例如：
     * <p>
     * /api/reading/tests/5
     * <p>
     * 那么这里的 id 就是 5。
     */
    @GetMapping("/{id}")
    public ReadingTest findById(
            @PathVariable Long id
    ) {
        return readingTestService.findById(id);
    }

    /**
     * 创建一个新的 Reading Test。
     * <p>
     * HTTP:
     * POST /api/reading/tests
     *
     * @RequestBody 表示 Spring 会把客户端发送的 JSON
     * 转换成 ReadingTest Java 对象。
     */
    @PostMapping
    public ReadingTest create(
            @RequestBody ReadingTest readingTest
    ) {
        return readingTestService.create(readingTest);
    }

    /**
     * 查询完整 ReadingTest。
     *
     * GET /api/reading/tests/1/full
     */
    @GetMapping("/{id}/full")
    public ReadingTestDetailResponse getReadingTestDetail(
            @PathVariable Long id
    ) {
        return readingTestService.findDetailById(id);
    }

    /**
     * 提交一整套 Reading Test 的答案并进行判分。
     *
     * HTTP：
     *
     * POST /api/reading/tests/{id}/submit
     *
     * 例如：
     *
     * POST /api/reading/tests/7/submit
     *
     * 请求 JSON：
     *
     * {
     *   "answers": {
     *     "27": "viii",
     *     "28": "iv",
     *     "35": "D"
     *   }
     * }
     *
     * 调用链：
     *
     * Frontend
     * ↓
     * ReadingTestController
     * ↓
     * ReadingSubmissionService
     * ↓
     * ReadingQuestionRepository
     * ↓
     * SQLite 中的 correctAnswer
     * ↓
     * ReadingSubmitResponse
     */
    @PostMapping("/{id}/submit")
    public ReadingSubmitResponse submitReadingTest(
            @PathVariable Long id,
            @RequestBody ReadingSubmitRequest request
    ) {
        return readingSubmissionService.submitTest(id, request);
    }
}
