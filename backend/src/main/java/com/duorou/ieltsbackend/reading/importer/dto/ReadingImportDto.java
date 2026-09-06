package com.duorou.ieltsbackend.reading.importer.dto;

import java.util.List;

/**
 * ReadingImportDto
 *
 * 一套 Reading Test 导入数据的最外层 DTO。
 *
 * 它最终对应：
 *
 * Reading Test
 *     ↓
 * Passage
 *     ↓
 * Question
 */
public class ReadingImportDto {

    /**
     * 外部题库自己的唯一编号。
     *
     * 例如原仓库：
     * p1-high-01
     *
     * 以后可以利用这个字段防止重复导入。
     */
    private String externalId;

    /**
     * Reading Test 标题。
     */
    private String title;

    /**
     * 数据来源。
     *
     * 例如：
     * IELTS Practice Dataset
     */
    private String source;

    /**
     * 当前 Test 包含的 Passage。
     */
    private List<ReadingPassageImportDto> passages;


    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<ReadingPassageImportDto> getPassages() {
        return passages;
    }

    public void setPassages(List<ReadingPassageImportDto> passages) {
        this.passages = passages;
    }
}
