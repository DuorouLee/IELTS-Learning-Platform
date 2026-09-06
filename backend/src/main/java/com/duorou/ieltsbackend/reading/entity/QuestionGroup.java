package com.duorou.ieltsbackend.reading.entity;

import jakarta.persistence.*;

/**
 * QuestionGroup 表示 IELTS Reading 中的一组题目。
 *
 * 为什么需要 QuestionGroup？
 *
 * IELTS 中经常会出现：
 *
 * Questions 14-18
 * Choose the correct heading for each paragraph.
 *
 * 这里的 instruction 并不是某一道题自己的，
 * 而是 14-18 这一整组题共享的说明。
 *
 * 因此数据结构从：
 *
 * ReadingPassage
 *      ↓
 * ReadingQuestion
 *
 * 升级为：
 *
 * ReadingPassage
 *      ↓
 * QuestionGroup
 *      ↓
 * ReadingQuestion
 */
@Entity
@Table(name = "question_group")
public class QuestionGroup {

    /**
     * QuestionGroup 的数据库主键。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 当前题组属于哪一个 ReadingPassage。
     *
     * 一个 Passage 可以拥有多个 QuestionGroup。
     *
     * 例如：
     *
     * Passage 1
     *
     * Group 1:
     * Questions 1-5
     * TRUE_FALSE_NOT_GIVEN
     *
     * Group 2:
     * Questions 6-13
     * MATCHING_HEADINGS
     */
    @ManyToOne
    @JoinColumn(name = "passage_id", nullable = false)
    private ReadingPassage readingPassage;

    /**
     * 当前题组的题型。
     *
     * 例如：
     *
     * TRUE_FALSE_NOT_GIVEN
     * MATCHING_HEADINGS
     * MATCHING_FEATURES
     * MULTIPLE_CHOICE
     */
    @Column(name = "question_type", nullable = false)
    private String questionType;

    /**
     * IELTS 原题中的答题说明。
     *
     * 例如：
     *
     * "Choose the correct heading for each paragraph
     * from the list of headings below."
     *
     * instruction 属于整个题组，
     * 所以放在 QuestionGroup，
     * 而不是 ReadingQuestion。
     */
    @Column(name = "instruction", columnDefinition = "TEXT")
    private String instruction;

    /**
     * 是否允许重复使用选项。
     *
     * 某些 IELTS Matching 类型会写：
     *
     * "NB You may use any letter more than once."
     *
     * true  = 一个选项可以被多个题目使用
     * false = 一个选项只能使用一次
     */
    @Column(name = "allow_option_reuse", nullable = false)
    private Boolean allowOptionReuse = false;

    /**
     * 无参构造函数。
     *
     * JPA / Hibernate 创建对象时需要。
     */
    public QuestionGroup() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReadingPassage getReadingPassage() {
        return readingPassage;
    }

    public void setReadingPassage(ReadingPassage readingPassage) {
        this.readingPassage = readingPassage;
    }

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
}
