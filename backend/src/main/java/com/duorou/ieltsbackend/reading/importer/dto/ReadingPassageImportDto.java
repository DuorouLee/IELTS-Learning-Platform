package com.duorou.ieltsbackend.reading.importer.dto;

import java.util.List;

/**
 * ReadingPassageImportDto
 *
 * 用来接收一篇 Reading Passage 的 JSON 数据。
 */
public class ReadingPassageImportDto {

    private Integer passageNumber;

    private String title;

    /**
     * 当前 Passage 的公共说明文字。
     *
     * 例如：
     *
     * You should spend about 20 minutes on Questions 1-13,
     * which are based on Reading Passage 1 on the following pages.
     *
     * 这个字段会从导入 JSON 中读取，
     * 后面再由 ReadingImportService 写入 ReadingPassage Entity。
     */
    private String instruction;

    private String content;

    /**
     * 新增：
     * 当前 Passage 下的所有题组。
     *
     * 例如：
     *
     * Passage 1
     *   ├── QuestionGroup 1
     *   └── QuestionGroup 2
     */
    private List<QuestionGroupImportDto> questionGroups;

    /**
     * 旧结构先保留。
     *
     * 这样旧 JSON 仍然可以继续导入，
     * 不会因为结构升级立刻全部失效。
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

    public List<QuestionGroupImportDto> getQuestionGroups() {
        return questionGroups;
    }

    public void setQuestionGroups(List<QuestionGroupImportDto> questionGroups) {
        this.questionGroups = questionGroups;
    }

    public List<ReadingQuestionImportDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<ReadingQuestionImportDto> questions) {
        this.questions = questions;
    }

    /**
     * 获取 Passage 公共说明。
     */
    public String getInstruction() {
        return instruction;
    }

    /**
     * 设置 Passage 公共说明。
     */
    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }
}
