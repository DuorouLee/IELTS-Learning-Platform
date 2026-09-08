package com.duorou.ieltsbackend.reading.service;

import com.duorou.ieltsbackend.reading.dto.ReadingPracticeRecordResponse;
import com.duorou.ieltsbackend.reading.entity.ReadingPracticeRecord;
import com.duorou.ieltsbackend.reading.repository.ReadingPracticeRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * readOnly = true：
     * 这个事务只负责读取数据。
     *
     * 在整个方法执行期间，
     * Hibernate Session 会保持开启，
     * 所以 LAZY 的 ReadingTest 可以正常读取。
     */
    @Transactional(readOnly = true)
    public List<ReadingPracticeRecordResponse> getPracticeHistory() {

        return readingPracticeRecordRepository
                .findAllByOrderBySubmittedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

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

    /**
     * 删除一条 Reading Practice History。
     *
     * @param recordId 历史记录主键
     */
    public void deletePracticeRecord(Long recordId) {

        /**
         * 先确认记录存在。
         *
         * 如果不存在，就抛出异常。
         */
        if (!readingPracticeRecordRepository.existsById(recordId)) {
            throw new IllegalArgumentException(
                    "Reading practice record not found: " + recordId
            );
        }

        /**
         * 删除记录。
         */
        readingPracticeRecordRepository.deleteById(recordId);
    }
}
