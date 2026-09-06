package com.duorou.ieltsbackend.reading.importer.dto;

import java.util.List;

/**
 * ReadingQuestionImportDto
 *
 * 作用：
 * 用来接收“外部题库”里的 Question 数据。
 *
 * 注意：
 * 这个类不是数据库 Entity。
 *
 * 它只是：
 *
 * 外部 JSON
 *      ↓
 * Import DTO
 *      ↓
 * Service
 *      ↓
 * Entity
 *      ↓
 * Database
 *
 * 这样可以把“外部题库格式”和“内部数据库结构”分开。
 */
public class ReadingQuestionImportDto {

    /**
     * IELTS 题号。
     *
     * 例如：
     * 1
     * 2
     * 3
     */
    private Integer questionNumber;

    /**
     * 题型。
     *
     * 例如：
     * MATCHING_HEADINGS
     * TRUE_FALSE_NOT_GIVEN
     * MULTIPLE_CHOICE
     */
    private String questionType;

    /**
     * 题目正文。
     */
    private String questionText;

    /**
     * 标准答案。
     *
     * 例如：
     * TRUE
     * FALSE
     * viii
     * D
     */
    private String correctAnswer;

    /**
     * 题目解析。
     *
     * 当前原始题库不一定都有解析，
     * 所以这个字段以后可以允许为空。
     */
    private String explanation;

    /**
     * 当前题目的可选项。
     *
     * 例如 Matching Headings：
     *
     * i
     * ii
     * iii
     * iv
     *
     * 或 Multiple Choice：
     *
     * A
     * B
     * C
     * D
     */
    private List<String> options;


    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(Integer questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }
}
