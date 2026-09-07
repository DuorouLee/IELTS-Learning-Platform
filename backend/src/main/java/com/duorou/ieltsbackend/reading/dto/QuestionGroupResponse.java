package com.duorou.ieltsbackend.reading.dto;

import java.util.List;

/**
 * QuestionGroupResponse
 *
 * 用于 API 返回一个完整题组。
 *
 * 例如：
 *
 * Questions 1-8
 * MATCHING_HEADINGS
 *
 * instruction
 *      ↓
 * options
 *      ↓
 * questions
 */
public class QuestionGroupResponse {

    private Long id;

    private String questionType;

    private String instruction;

    private Boolean allowOptionReuse;

    private List<QuestionOptionResponse> options;

    private List<ReadingQuestionResponse> questions;

    public QuestionGroupResponse(
            Long id,
            String questionType,
            String instruction,
            Boolean allowOptionReuse,
            List<QuestionOptionResponse> options,
            List<ReadingQuestionResponse> questions
    ) {
        this.id = id;
        this.questionType = questionType;
        this.instruction = instruction;
        this.allowOptionReuse = allowOptionReuse;
        this.options = options;
        this.questions = questions;
    }

    public Long getId() {
        return id;
    }

    public String getQuestionType() {
        return questionType;
    }

    public String getInstruction() {
        return instruction;
    }

    public Boolean getAllowOptionReuse() {
        return allowOptionReuse;
    }

    public List<QuestionOptionResponse> getOptions() {
        return options;
    }

    public List<ReadingQuestionResponse> getQuestions() {
        return questions;
    }
}
