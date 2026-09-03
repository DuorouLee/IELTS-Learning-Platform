package com.duorou.ieltsbackend.reading.dto;

/**
 * ReadingQuestionResponse
 *
 * 专门用于 API 返回 Question 数据。
 *
 * 这样就不会把整个 ReadingPassage、
 * ReadingTest 一层一层重复返回。
 */
public class ReadingQuestionResponse {

    private Long id;

    private Integer questionNumber;

    private String questionType;

    private String questionText;

    private String correctAnswer;

    private String explanation;

    public ReadingQuestionResponse() {
    }

    public ReadingQuestionResponse(
            Long id,
            Integer questionNumber,
            String questionType,
            String questionText,
            String correctAnswer,
            String explanation
    ) {
        this.id = id;
        this.questionNumber = questionNumber;
        this.questionType = questionType;
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
    }

    public Long getId() {
        return id;
    }

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public String getQuestionType() {
        return questionType;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getExplanation() {
        return explanation;
    }
}
