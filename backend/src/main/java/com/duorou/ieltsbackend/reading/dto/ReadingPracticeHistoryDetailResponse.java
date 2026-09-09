package com.duorou.ieltsbackend.reading.dto;

import java.util.List;

/**
 * 某一次 Reading Practice 的完整历史详情。
 *
 * 用于：
 *
 * GET /api/reading/practice-history/{id}
 */
public class ReadingPracticeHistoryDetailResponse {

    private final Long id;
    private final Long testId;
    private final String testTitle;
    private final Integer correctCount;
    private final Integer totalQuestions;
    private final Double percentage;
    private final Long submittedAt;

    /**
     * 这一次练习中每一道题的历史作答结果。
     */
    private final List<ReadingPracticeAnswerResponse> answers;

    public ReadingPracticeHistoryDetailResponse(
            Long id,
            Long testId,
            String testTitle,
            Integer correctCount,
            Integer totalQuestions,
            Double percentage,
            Long submittedAt,
            List<ReadingPracticeAnswerResponse> answers
    ) {
        this.id = id;
        this.testId = testId;
        this.testTitle = testTitle;
        this.correctCount = correctCount;
        this.totalQuestions = totalQuestions;
        this.percentage = percentage;
        this.submittedAt = submittedAt;
        this.answers = answers;
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

    public List<ReadingPracticeAnswerResponse> getAnswers() {
        return answers;
    }
}
