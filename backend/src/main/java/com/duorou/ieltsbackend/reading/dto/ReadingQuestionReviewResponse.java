package com.duorou.ieltsbackend.reading.dto;

/**
 * 用户提交 Reading Test 后，
 * 单道题目的 Review 结果。
 *
 * 例如：
 *
 * Question 9
 * Your answer: F
 * Correct answer: D
 * Incorrect
 */
public class ReadingQuestionReviewResponse {

    /**
     * 数据库中的 Question ID。
     *
     * 前端用它找到对应的题目。
     */
    private final Long questionId;

    /**
     * IELTS 原始题号，例如 1、2、9、13。
     */
    private final Integer questionNumber;

    /**
     * 用户提交的答案。
     *
     * 如果没有作答，可以为 null。
     */
    private final String userAnswer;

    /**
     * 正确答案。
     *
     * 这个字段只应该在提交之后返回。
     */
    private final String correctAnswer;

    /**
     * 当前题是否回答正确。
     */
    private final boolean correct;

    public ReadingQuestionReviewResponse(
            Long questionId,
            Integer questionNumber,
            String userAnswer,
            String correctAnswer,
            boolean correct
    ) {
        this.questionId = questionId;
        this.questionNumber = questionNumber;
        this.userAnswer = userAnswer;
        this.correctAnswer = correctAnswer;
        this.correct = correct;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public boolean isCorrect() {
        return correct;
    }
}
