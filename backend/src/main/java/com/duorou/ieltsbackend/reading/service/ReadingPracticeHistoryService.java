package com.duorou.ieltsbackend.reading.service;

import com.duorou.ieltsbackend.reading.dto.ReadingPracticeRecordResponse;
import com.duorou.ieltsbackend.reading.entity.ReadingPracticeRecord;
import com.duorou.ieltsbackend.reading.repository.ReadingPracticeRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ReadingPracticeHistoryService
 *
 * 负责读取 Reading Practice History，
 * 并把数据库 Entity 转换成返回给前端的 DTO。
 */
@Service
public class ReadingPracticeHistoryService {

    private final ReadingPracticeRecordRepository readingPracticeRecordRepository;

    public ReadingPracticeHistoryService(
            ReadingPracticeRecordRepository readingPracticeRecordRepository
    ) {
        this.readingPracticeRecordRepository = readingPracticeRecordRepository;
    }

    /**
     * 获取全部 Reading 练习记录，
     * 按 submittedAt 从新到旧排序。
     */
    public List<ReadingPracticeRecordResponse> getPracticeHistory() {

        return readingPracticeRecordRepository
                .findAllByOrderBySubmittedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 把数据库 Entity 转成 API Response DTO。
     */
    private ReadingPracticeRecordResponse toResponse(
            ReadingPracticeRecord record
    ) {
        return new ReadingPracticeRecordResponse(
                record.getId(),
                record.getReadingTest().getId(),
                record.getReadingTest().getTitle(),
                record.getCorrectCount(),
                record.getTotalQuestions(),
                record.getPercentage(),
                record.getSubmittedAt()
        );
    }
}
