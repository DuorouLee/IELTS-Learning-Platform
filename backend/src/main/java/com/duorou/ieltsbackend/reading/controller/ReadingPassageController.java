package com.duorou.ieltsbackend.reading.controller;

import com.duorou.ieltsbackend.reading.entity.ReadingPassage;
import com.duorou.ieltsbackend.reading.service.ReadingPassageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ReadingPassageController
 * <p>
 * 作用：
 * 接收前端发送过来的 HTTP 请求，
 * 然后调用 ReadingPassageService。
 * <p>
 * 调用链路：
 * <p>
 * 浏览器 / 前端
 * ↓
 * ReadingPassageController
 * ↓
 * ReadingPassageService
 * ↓
 * ReadingPassageRepository
 * ↓
 * SQLite
 */
@RestController
@RequestMapping("/api/reading")
public class ReadingPassageController {

    /**
     * Service 负责具体业务逻辑。
     */
    private final ReadingPassageService readingPassageService;

    /**
     * 构造器注入。
     * <p>
     * Spring 会自动找到 ReadingPassageService，
     * 并传入这里。
     */
    public ReadingPassageController(
            ReadingPassageService readingPassageService
    ) {
        this.readingPassageService = readingPassageService;
    }

    /**
     * 查询全部 Passage
     * <p>
     * 请求：
     * GET /api/reading/passages
     */
    @GetMapping("/passages")
    public List<ReadingPassage> getAllPassages() {
        return readingPassageService.findAll();
    }

    /**
     * 根据 id 查询 Passage
     * <p>
     * 例如：
     * GET /api/reading/passages/1
     */
    @GetMapping("/passages/{id}")
    public ReadingPassage getPassageById(
            @PathVariable Long id
    ) {
        return readingPassageService.findById(id);
    }

    /**
     * 创建一个新的 Passage
     * <p>
     * 请求：
     * POST /api/reading/passages
     *
     * @RequestBody： 把前端发送过来的 JSON
     * 自动转换成 ReadingPassage Java 对象。
     */
    @PostMapping("/passages")
    public ReadingPassage createPassage(
            @RequestBody ReadingPassage readingPassage
    ) {
        return readingPassageService.create(readingPassage);
    }

    /**
     * 查询某个 ReadingTest 下的所有 Passage。
     * <p>
     * 例如：
     * GET /api/reading/tests/1/passages
     */
    @GetMapping("/tests/{testId}/passages")
    public List<ReadingPassage> getPassagesByTestId(
            @PathVariable Long testId
    ) {
        return readingPassageService.findByTestId(testId);
    }
}
