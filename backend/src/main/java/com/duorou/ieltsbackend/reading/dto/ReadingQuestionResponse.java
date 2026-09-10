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

    private String explanation;


    /**
     * 当前题目自己的独立选项。
     *
     * 数据库中目前保存为 JSON 字符串：
     *
     * ["Option A","Option B","Option C","Option D"]
     *
     * 这一轮先直接返回 String，
     * 下一步前端再决定是否解析成数组。
     */
    private String optionsJson;

    /**
     * 当前题目对应的原文高亮信息。
     *
     * 后续用于：
     * - Review 原文定位
     * - 答案句高亮
     */
    private String answerHighlightJson;

    public ReadingQuestionResponse(
            Long id,
            Integer questionNumber,
            String questionType,
            String questionText,
            String explanation,
            String optionsJson,
            String answerHighlightJson
    ) {
        this.id = id;
        this.questionNumber = questionNumber;
        this.questionType = questionType;
        this.questionText = questionText;
        this.explanation = explanation;
        this.optionsJson = optionsJson;
        this.answerHighlightJson = answerHighlightJson;
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


    public String getExplanation() {
        return explanation;
    }

    public String getOptionsJson() {
        return optionsJson;
    }

    public String getAnswerHighlightJson() {
        return answerHighlightJson;
    }
}
