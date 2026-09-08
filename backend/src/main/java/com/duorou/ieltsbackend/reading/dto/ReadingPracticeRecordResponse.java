package com.duorou.ieltsbackend.reading.dto;

import java.time.LocalDateTime;

/**
 * 返回给前端的一条 Reading Practice History。
 */
public class ReadingPracticeRecordResponse {

    private final Long id;
    private final Long testId;
    private final String testTitle;
    private final Integer correctCount;
    private final Integer totalQuestions;
    private final Double percentage;
    private final Long submittedAt;

    public ReadingPracticeRecordResponse(
            Long id,
            Long testId,
            String testTitle,
            Integer correctCount,
            Integer totalQuestions,
            Double percentage,
            Long submittedAt
    ) {
        this.id = id;
        this.testId = testId;
        this.testTitle = testTitle;
        this.correctCount = correctCount;
        this.totalQuestions = totalQuestions;
        this.percentage = percentage;
        this.submittedAt = submittedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getTestId() {
        return testId;
    }

    public String getTestTitle() {
        return testTitle;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public Double getPercentage() {
        return percentage;
    }

    public Long getSubmittedAt() {
        return submittedAt;
    }
}
