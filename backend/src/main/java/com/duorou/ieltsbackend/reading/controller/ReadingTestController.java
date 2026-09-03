package com.duorou.ieltsbackend.reading.controller;

import com.duorou.ieltsbackend.reading.dto.ReadingTestDetailResponse;
import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import com.duorou.ieltsbackend.reading.service.ReadingTestService;
import org.springframework.web.bind.annotation.*;

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
     * 构造器注入。
     * <p>
     * Spring 会自动把 ReadingTestService
     * 注入到 Controller 中。
     */
    public ReadingTestController(
            ReadingTestService readingTestService
    ) {
        this.readingTestService = readingTestService;
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
}
