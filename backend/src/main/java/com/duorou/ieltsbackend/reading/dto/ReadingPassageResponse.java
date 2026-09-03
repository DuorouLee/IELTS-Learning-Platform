package com.duorou.ieltsbackend.reading.dto;

import java.util.List;

/**
 * ReadingPassageResponse
 *
 * 专门用于 API 返回 Passage 数据。
 *
 * 目的：
 * 不再把整个 ReadingTest 对象重复嵌套到每一个 Passage 里面。
 */
public class ReadingPassageResponse {

    private Long id;

    private Integer passageNumber;

    private String title;

    private String content;

    private List<ReadingQuestionResponse> questions;

    public ReadingPassageResponse(
            Long id,
            Integer passageNumber,
            String title,
            String content,
            List<ReadingQuestionResponse> questions
    ) {
        this.id = id;
        this.passageNumber = passageNumber;
        this.title = title;
        this.content = content;
        this.questions = questions;
    }

    public ReadingPassageResponse(
            Long id,
            Integer passageNumber,
            String title,
            String content
    ) {
        this.id = id;
        this.passageNumber = passageNumber;
        this.title = title;
        this.content = content;
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

    public String getContent() {
        return content;
    }

    public List<ReadingQuestionResponse> getQuestions() {
        return questions;
    }
}
