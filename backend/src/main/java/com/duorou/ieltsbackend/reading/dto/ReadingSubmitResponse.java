package com.duorou.ieltsbackend.reading.dto;

import java.util.List;

/**
 * Reading Test 提交后的完整结果。
 *
 * 包含：
 * 1. 总题数
 * 2. 正确题数
 * 3. 错误题数
 * 4. 正确率
 * 5. 每一道题的 Review 结果
 */
public class ReadingSubmitResponse {

    private final int totalQuestions;

    private final int correctCount;

    private final int incorrectCount;

    private final double percentage;

    /**
     * 每一道题的提交结果。
     *
     * 例如：
     * Question 9
     * 用户答案 F
     * 正确答案 D
     * correct = false
     */
    private final List<ReadingQuestionReviewResponse> questions;

    public ReadingSubmitResponse(
            int totalQuestions,
            int correctCount,
            int incorrectCount,
            double percentage,
            List<ReadingQuestionReviewResponse> questions
    ) {
        this.totalQuestions = totalQuestions;
        this.correctCount = correctCount;
        this.incorrectCount = incorrectCount;
        this.percentage = percentage;
        this.questions = questions;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getIncorrectCount() {
        return incorrectCount;
    }

    public double getPercentage() {
        return percentage;
    }

    public List<ReadingQuestionReviewResponse> getQuestions() {
        return questions;
    }
}
