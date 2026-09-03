package com.duorou.ieltsbackend.reading.entity;

import jakarta.persistence.*;

/**
 * ReadingPassage 对应数据库中的 reading_passage 表。
 *
 * 一条 ReadingPassage 数据代表一篇 IELTS Reading 文章。
 *
 * 它属于某一个 ReadingTest。
 */
@Entity
@Table(name = "reading_passage")
public class ReadingPassage {

    /**
     * Passage 自己的主键。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 当前 Passage 属于哪一个 Reading Test。
     *
     * 数据库里面原本是：
     *
     * test_id INTEGER NOT NULL
     *
     * 但是在 Java 里，我们不希望只保存一个数字：
     *
     * Long testId;
     *
     * 我们更希望直接表达：
     *
     * ReadingPassage 属于一个 ReadingTest。
     *
     * 所以这里写成：
     *
     * private ReadingTest readingTest;
     *
     *
     * @ManyToOne
     *
     * 表示：
     *
     * 很多个 Passage
     * 可以属于
     * 一个 ReadingTest
     *
     *
     * @JoinColumn(name = "test_id")
     *
     * 表示它们之间通过数据库里的
     * test_id 字段连接。
     */
    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private ReadingTest readingTest;

    /**
     * Passage 编号。
     *
     * 例如：
     * 1
     * 2
     * 3
     */
    @Column(name = "passage_number", nullable = false)
    private Integer passageNumber;

    /**
     * 文章标题。
     */
    private String title;

    /**
     * Reading 正文。
     *
     * columnDefinition = "TEXT"
     *
     * 是为了告诉数据库：
     * 这里保存的是比较长的文本内容。
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public ReadingPassage() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReadingTest getReadingTest() {
        return readingTest;
    }

    public void setReadingTest(ReadingTest readingTest) {
        this.readingTest = readingTest;
    }

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
}
