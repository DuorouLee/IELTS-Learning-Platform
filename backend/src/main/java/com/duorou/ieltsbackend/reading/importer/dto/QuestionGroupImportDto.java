package com.duorou.ieltsbackend.reading.importer.dto;

import java.util.List;

/**
 * QuestionGroupImportDto
 *
 * 用来接收 JSON 里的一组 Reading 题。
 *
 * 例如：
 *
 * Questions 14-18
 * Choose the correct heading for each paragraph.
 *
 * 这一组会共享：
 * - questionType
 * - instruction
 * - allowOptionReuse
 * - options
 */
public class QuestionGroupImportDto {

    /**
     * 当前题组类型。
     *
     * 例如：
     * MATCHING_HEADINGS
     * MATCHING_FEATURES
     */
    private String questionType;

    /**
     * 当前题组共享的答题说明。
     */
    private String instruction;

    /**
     * 是否允许重复使用选项。
     */
    private Boolean allowOptionReuse;

    /**
     * 当前题组的所有选项。
     */
    private List<QuestionOptionImportDto> options;

    /**
     * 当前题组包含的所有问题。
     */
    private List<ReadingQuestionImportDto> questions;

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public Boolean getAllowOptionReuse() {
        return allowOptionReuse;
    }

    public void setAllowOptionReuse(Boolean allowOptionReuse) {
        this.allowOptionReuse = allowOptionReuse;
    }

    public List<QuestionOptionImportDto> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOptionImportDto> options) {
        this.options = options;
    }

    public List<ReadingQuestionImportDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<ReadingQuestionImportDto> questions) {
        this.questions = questions;
    }
}
