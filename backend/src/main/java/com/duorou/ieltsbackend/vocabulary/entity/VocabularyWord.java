package com.duorou.ieltsbackend.vocabulary.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * VocabularyWord 对应数据库中的 vocabulary_word 表。
 *
 * 一条 VocabularyWord 数据代表词库中的一个英语单词。
 *
 * 例如：
 *
 * word = "abandon"
 * meaning = "放弃；抛弃"
 * exampleSentence = "The project was abandoned because of a lack of funding."
 */
@Entity
@Table(name = "vocabulary_word")
public class VocabularyWord {

    /**
     * 数据库主键。
     *
     * 对应：
     *
     * id INTEGER PRIMARY KEY AUTOINCREMENT
     *
     * @Id
     * 表示这是 Entity 的主键。
     *
     * GenerationType.IDENTITY
     * 表示 id 由数据库自动生成，
     * 创建单词时不需要我们自己设置。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 英语单词本身。
     *
     * 例如：
     *
     * abandon
     *
     * nullable = false：
     * 对应数据库中的 NOT NULL。
     *
     * unique = true：
     * 同一个单词不能重复保存。
     */
    @Column(nullable = false, unique = true)
    private String word;

    /**
     * 单词释义。
     *
     * 例如：
     *
     * 放弃；抛弃
     *
     * 当前 Vocabulary 第一阶段只保存一个主要释义。
     * 后面如果需要多词义结构，再继续扩展。
     */
    @Column(nullable = false)
    private String meaning;

    /**
     * 示例句。
     *
     * 对应数据库字段：
     *
     * example_sentence
     *
     * 数据库允许它为空，
     * 所以这里不写 nullable = false。
     */
    @Column(name = "example_sentence")
    private String exampleSentence;

    /**
     * 创建时间。
     *
     * insertable = false：
     * Hibernate INSERT 时不主动传这个字段，
     * 让 SQLite 使用：
     *
     * DEFAULT CURRENT_TIMESTAMP
     *
     * 自动生成时间。
     *
     * updatable = false：
     * 后续修改单词时，不修改创建时间。
     */
    @Column(
            name = "created_at",
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    /**
     * JPA / Hibernate 需要无参构造方法。
     *
     * Hibernate 从数据库读取 vocabulary_word 后，
     * 会使用它创建 VocabularyWord Java 对象。
     */
    public VocabularyWord() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getExampleSentence() {
        return exampleSentence;
    }

    public void setExampleSentence(String exampleSentence) {
        this.exampleSentence = exampleSentence;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
