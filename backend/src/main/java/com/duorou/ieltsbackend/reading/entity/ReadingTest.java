package com.duorou.ieltsbackend.reading.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * ReadingTest 对应数据库中的 reading_test 表。
 *
 * 一条 ReadingTest 数据代表一整套 IELTS Reading 测试，
 * 例如：
 *
 * Cambridge IELTS 18 Test 1
 */
@Entity
@Table(name = "reading_test")
public class ReadingTest {

    /**
     * 主键。
     *
     * 对应数据库：
     *
     * id INTEGER PRIMARY KEY AUTOINCREMENT
     *
     * @Id
     * 表示这个字段是数据库主键。
     *
     * @GeneratedValue
     * 表示 id 不需要我们自己填写，
     * 数据库会自动生成。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 测试名称。
     *
     * 例如：
     * Cambridge IELTS 18 Test 1
     *
     * nullable = false
     * 对应数据库里的 NOT NULL。
     */
    @Column(nullable = false)
    private String title;

    /**
     * 来源。
     *
     * 例如：
     * Cambridge IELTS 18
     */
    private String source;

    /**
     * 创建时间。
     *
     * 数据库字段名是：
     * created_at
     *
     * Java 使用驼峰命名：
     * createdAt
     *
     * 所以通过 @Column 告诉 JPA
     * 它们实际上是同一个字段。
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * JPA 需要一个无参构造方法。
     *
     * 现在你可以先理解成：
     * Hibernate 从数据库读取数据后，
     * 需要通过这个构造方法创建 Java 对象。
     */
    public ReadingTest() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
