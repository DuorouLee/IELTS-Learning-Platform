package com.duorou.ieltsbackend.reading.service;

import com.duorou.ieltsbackend.reading.dto.ReadingPracticeRecordResponse;
import com.duorou.ieltsbackend.reading.entity.ReadingPracticeRecord;
import com.duorou.ieltsbackend.reading.repository.ReadingPracticeRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.duorou.ieltsbackend.reading.dto.ReadingPracticeAnswerResponse;
import com.duorou.ieltsbackend.reading.dto.ReadingPracticeHistoryDetailResponse;
import com.duorou.ieltsbackend.reading.entity.ReadingPracticeAnswer;
import com.duorou.ieltsbackend.reading.repository.ReadingPracticeAnswerRepository;

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

    /**
     * 用来读取某一次 Practice Record
     * 对应的所有逐题答案。
     */
    private final ReadingPracticeAnswerRepository readingPracticeAnswerRepository;

    public ReadingPracticeHistoryService(
            ReadingPracticeRecordRepository readingPracticeRecordRepository,
            ReadingPracticeAnswerRepository readingPracticeAnswerRepository
    ) {
        this.readingPracticeRecordRepository = readingPracticeRecordRepository;
        this.readingPracticeAnswerRepository = readingPracticeAnswerRepository;
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
     * 查询某一次 Reading Practice 的完整详情。
     *
     * 包括：
     *
     * - 本次 Test
     * - 总成绩
     * - 提交时间
     * - 每一道题当时的答案
     */
    @Transactional(readOnly = true)
    public ReadingPracticeHistoryDetailResponse getPracticeHistoryDetail(
            Long recordId
    ) {

        /*
         * 第一步：
         * 查询 Practice Record。
         */
        ReadingPracticeRecord record =
                readingPracticeRecordRepository
                        .findById(recordId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Reading practice record not found: " + recordId
                                )
                        );


        /*
         * 第二步：
         * 查询这一次练习对应的所有逐题答案。
         *
         * Repository 会按照 questionNumber
         * 从小到大排序。
         */
        List<ReadingPracticeAnswerResponse> answers =
                readingPracticeAnswerRepository
                        .findByPracticeRecordIdOrderByQuestionNumberAsc(recordId)
                        .stream()
                        .map(this::toAnswerResponse)
                        .toList();


        /*
         * 第三步：
         * 把 Entity 转换成 Detail DTO。
         */
        return new ReadingPracticeHistoryDetailResponse(
                record.getId(),
                record.getReadingTest().getId(),
                record.getReadingTest().getTitle(),
                record.getCorrectCount(),
                record.getTotalQuestions(),
                record.getPercentage(),
                record.getSubmittedAt(),
                answers
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

    /**
     * 把 ReadingPracticeAnswer Entity
     * 转换成 API DTO。
     */
    private ReadingPracticeAnswerResponse toAnswerResponse(
            ReadingPracticeAnswer answer
    ) {
        return new ReadingPracticeAnswerResponse(
                answer.getQuestionId(),
                answer.getQuestionNumber(),
                answer.getUserAnswer(),
                answer.getCorrectAnswer(),
                answer.isCorrect()
        );
    }
}
