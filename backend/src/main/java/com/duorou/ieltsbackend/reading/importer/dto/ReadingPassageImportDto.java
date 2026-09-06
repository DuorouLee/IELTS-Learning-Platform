package com.duorou.ieltsbackend.reading.importer.dto;

import java.util.List;

/**
 * ReadingPassageImportDto
 *
 * 作用：
 * 表示从外部题库导入的一篇 Reading Passage。
 *
 * 数据流：
 *
 * 外部 JSON
 *     ↓
 * ReadingPassageImportDto
 *     ↓
 * Import Service
 *     ↓
 * ReadingPassage Entity
 *     ↓
 * SQLite
 */
public class ReadingPassageImportDto {

    /**
     * Passage 编号。
     *
     * IELTS Reading 通常为：
     * Passage 1
     * Passage 2
     * Passage 3
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
     * 阅读文章正文。
     */
    private String content;

    /**
     * 当前 Passage 下的所有题目。
     */
    private List<ReadingQuestionImportDto> questions;


    public Integer getPassageNumber() {
        return passageNumber;
    }

    public void setPassageNumber(Integer passageNumber) {
        this.passageNumber = passageNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<ReadingQuestionImportDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<ReadingQuestionImportDto> questions) {
        this.questions = questions;
    }
}
