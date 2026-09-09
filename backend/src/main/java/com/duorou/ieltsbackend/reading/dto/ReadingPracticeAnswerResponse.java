package com.duorou.ieltsbackend.reading.dto;

/**
 * Reading Practice History Detail 中，
 * 单道题的历史作答结果。
 */
public class ReadingPracticeAnswerResponse {

    private final Long questionId;
    private final Integer questionNumber;
    private final String userAnswer;
    private final String correctAnswer;
    private final boolean correct;

    public ReadingPracticeAnswerResponse(
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
