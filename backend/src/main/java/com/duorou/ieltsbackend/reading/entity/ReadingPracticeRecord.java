package com.duorou.ieltsbackend.reading.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * ReadingPracticeRecord
 *
 * 表示用户一次 Reading Test 的练习记录。
 *
 * 例如：
 * Test 7
 * 10 / 13
 * 76.92%
 * 2026-09-08 21:50
 */
@Entity
@Table(name = "reading_practice_record")
public class ReadingPracticeRecord {

    /**
     * 主键。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 这次练习对应哪一个 Reading Test。
     *
     * 多条练习记录可以属于同一个 Reading Test，
     * 所以这里使用 ManyToOne。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private ReadingTest readingTest;

    /**
     * 本次答对题数。
     */
    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;

    /**
     * 本次总题数。
     */
    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    /**
     * 本次正确率。
     *
     * 例如：
     * 76.923076...
     */
    @Column(nullable = false)
    private Double percentage;

    /**
     * 提交时间。
     */
    @Column(name = "submitted_at", nullable = false)
    private Long submittedAt;

    public Long getId() {
        return id;
    }

    public ReadingTest getReadingTest() {
        return readingTest;
    }

    public void setReadingTest(ReadingTest readingTest) {
        this.readingTest = readingTest;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public Long getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Long submittedAt) {
        this.submittedAt = submittedAt;
    }
}
