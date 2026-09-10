package com.duorou.ieltsbackend.reading.dto;

import java.util.List;

/**
 * ReadingPassageResponse
 *
 * 专门用于 API 返回 Passage 数据。
 *
 * 目的：
 * 不直接把 Entity 返回给前端，
 * 而是只返回前端真正需要的数据。
 */
public class ReadingPassageResponse {

    /**
     * Passage 主键 ID。
     */
    private Long id;

    /**
     * Passage 编号。
     *
     * 例如：
     * 1、2、3
     */
    private Integer passageNumber;

    /**
     * Passage 标题。
     *
     * 例如：
     * A Brief History of Tea
     */
    private String title;

    /**
     * Passage 的公共说明。
     *
     * 例如：
     *
     * You should spend about 20 minutes on Questions 1-13,
     * which are based on Reading Passage 1 on the following pages.
     *
     * 这个字段以后会显示在双栏区域上方。
     */
    private String instruction;

    /**
     * Passage 正文。
     */
    private String content;

    /**
     * Passage 中文译文。
     *
     * 来自数据库中的：
     * reading_passage.translation
     *
     * 前端后续可以通过“译文”开关显示或隐藏。
     */
    private String translation;

    /**
     * 当前 Passage 下的普通题目列表。
     */
    private List<ReadingQuestionResponse> questions;

    /**
     * 当前 Passage 下的 QuestionGroup。
     *
     * 例如：
     *
     * MATCHING_HEADINGS
     * MATCHING_FEATURES
     */
    private List<QuestionGroupResponse> questionGroups;

    /**
     * 完整构造方法。
     *
     * /full API 当前主要会使用这一版。
     */
    public ReadingPassageResponse(
            Long id,
            Integer passageNumber,
            String title,
            String instruction,
            String content,
            String translation,
            List<ReadingQuestionResponse> questions,
            List<QuestionGroupResponse> questionGroups
    ) {
        this.id = id;
        this.passageNumber = passageNumber;
        this.title = title;
        this.instruction = instruction;
        this.content = content;
        this.translation = translation;
        this.questions = questions;
        this.questionGroups = questionGroups;
    }

    /**
     * 简化构造方法。
     *
     * 某些只需要 Passage 基础信息的地方可以使用。
     */
    public ReadingPassageResponse(
            Long id,
            Integer passageNumber,
            String title,
            String instruction,
            String content,
            String translation
    ) {
        this.id = id;
        this.passageNumber = passageNumber;
        this.title = title;
        this.instruction = instruction;
        this.content = content;
        this.translation = translation;
    }

    public Long getId() {
        return id;
    }

    public Integer getPassageNumber() {
        return passageNumber;
    }

    public String getTitle() {
        return title;
    }

    /**
     * 返回 Passage 公共说明。
     */
    public String getInstruction() {
        return instruction;
    }

    public String getContent() {
        return content;
    }

    public List<ReadingQuestionResponse> getQuestions() {
        return questions;
    }

    public List<QuestionGroupResponse> getQuestionGroups() {
        return questionGroups;
    }

    public String getTranslation() {
        return translation;
    }
}
