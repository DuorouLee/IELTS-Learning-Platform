package com.duorou.ieltsbackend.reading.controller;

import com.duorou.ieltsbackend.reading.dto.ReadingPracticeRecordResponse;
import com.duorou.ieltsbackend.reading.service.ReadingPracticeHistoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Reading Practice History API。
 *
 * 负责提供 Reading 的历史练习记录。
 */
@RestController
@RequestMapping("/api/reading/practice-history")
public class ReadingPracticeHistoryController {

    private final ReadingPracticeHistoryService readingPracticeHistoryService;

    public ReadingPracticeHistoryController(
            ReadingPracticeHistoryService readingPracticeHistoryService
    ) {
        this.readingPracticeHistoryService = readingPracticeHistoryService;
    }

    /**
     * 获取全部 Reading Practice History。
     *
     * GET /api/reading/practice-history
     */
    @GetMapping
    public List<ReadingPracticeRecordResponse> getPracticeHistory() {
        return readingPracticeHistoryService.getPracticeHistory();
    }

    /**
     * 删除一条 Reading Practice History。
     *
     * DELETE /api/reading/practice-history/{id}
     */
    @DeleteMapping("/{id}")
    public void deletePracticeRecord(
            @PathVariable Long id
    ) {
        readingPracticeHistoryService.deletePracticeRecord(id);
    }
}
