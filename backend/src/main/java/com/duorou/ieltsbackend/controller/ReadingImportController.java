package com.duorou.ieltsbackend.reading.controller;

import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import com.duorou.ieltsbackend.reading.importer.ReadingImportService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * ReadingImportController
 *
 * 作用：
 * 提供一个 HTTP API，
 * 用来触发 Reading JSON 的数据库导入。
 *
 * 整体链路：
 *
 * HTTP Request
 *      ↓
 * ReadingImportController
 *      ↓
 * ReadingImportService
 *      ↓
 * Repository
 *      ↓
 * SQLite
 *
 * Controller 不负责具体的导入逻辑。
 * 真正的 JSON 读取、DTO 转 Entity、数据库保存，
 * 都继续由 ReadingImportService 负责。
 */
@RestController
@RequestMapping("/api/reading/import")
public class ReadingImportController {

    /**
     * Controller 依赖 ReadingImportService。
     *
     * 继续使用构造器注入，
     * 和我们项目其他 Service / Controller 的写法保持一致。
     */
    private final ReadingImportService readingImportService;

    public ReadingImportController(
            ReadingImportService readingImportService
    ) {
        this.readingImportService = readingImportService;
    }

    /**
     * 导入一份 Reading JSON。
     *
     * 请求示例：
     *
     * POST
     * /api/reading/import/p1-high-01.json
     *
     * fileName 会得到：
     *
     * p1-high-01.json
     *
     * 然后交给 ReadingImportService：
     *
     * resources/data/reading/p1-high-01.json
     *                  ↓
     *          ReadingImportService
     *                  ↓
     *               SQLite
     */
    @PostMapping("/{fileName}")
    @ResponseStatus(HttpStatus.CREATED)
    public ReadingTest importReading(
            @PathVariable String fileName
    ) {

        return readingImportService.importReading(fileName);
    }
}
