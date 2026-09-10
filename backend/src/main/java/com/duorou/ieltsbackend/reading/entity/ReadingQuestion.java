package com.duorou.ieltsbackend.reading.entity;

import jakarta.persistence.*;

/**
 * ReadingQuestion 对应数据库中的 reading_question 表。
 *
 * 一条数据代表 IELTS Reading 中的一道题目。
 */
@Entity
@Table(name = "reading_question")
public class ReadingQuestion {

    /**
     * Question 主键。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 当前 Question 属于哪一个 Passage。
     *
     * 数据库中对应：
     *
     * passage_id
     */
    @ManyToOne
    @JoinColumn(name = "passage_id", nullable = false)
    private ReadingPassage readingPassage;

    /**
     * 题号。
     *
     * 例如：
     * 1
     * 2
     * 3
     */
    @Column(name = "question_number", nullable = false)
    private Integer questionNumber;

    /**
     * 题型。
     *
     * 第一版我们暂时使用 String。
     *
     * 例如：
     *
     * TRUE_FALSE_NOT_GIVEN
     * MULTIPLE_CHOICE
     * MATCHING_HEADINGS
     */
    @Column(name = "question_type", nullable = false)
    private String questionType;

    /**
     * 题目正文。
     */
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    /**
     * 正确答案。
     */
    @Column(name = "correct_answer", nullable = false)
    private String correctAnswer;

    /**
     * 答案解析。
     *
     * 以后我们的 IELTS Learning Platform
     * 可以利用这个字段做学习模式。
     */
    @Column(columnDefinition = "TEXT")
    private String explanation;

    /**
     * 当前题目所属的题组 ID。
     *
     * 对应数据库 reading_question.group_id。
     *
     * 例如：
     *
     * QuestionGroup 1
     *   ├── Question 14
     *   ├── Question 15
     *   └── Question 16
     *
     * 这些题目的 groupId 都会指向同一个 question_group.id。
     *
     * 注意：
     * 目前旧数据可能还没有 group_id，
     * 所以这里使用 Long，而不是 long。
     *
     * Long 可以为 null，
     * long 不能为 null。
     */
    @Column(name = "group_id")
    private Long groupId;

    /**
     * 当前题目自己的独立选项。
     *
     * 主要用于 Multiple Choice。
     *
     * 数据库中保存为 JSON 字符串，例如：
     *
     * ["Option A","Option B","Option C","Option D"]
     *
     * 为什么不用 QuestionOption？
     *
     * QuestionOption 当前属于整个 QuestionGroup，
     * 适合 Matching 这种“一组题共享同一组选项”的情况。
     *
     * Multiple Choice 中每一道题的选项通常不同，
     * 所以需要保存到 ReadingQuestion 自己身上。
     */
    @Column(name = "options_json", columnDefinition = "TEXT")
    private String optionsJson;

    public ReadingQuestion() {
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

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(Integer questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    /**
     * 获取当前题目所属的题组 ID。
     */
    public Long getGroupId() {
        return groupId;
    }

    /**
     * 设置当前题目所属的题组 ID。
     */
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    /**
     * 获取当前题目的独立选项 JSON。
     */
    public String getOptionsJson() {
        return optionsJson;
    }

    /**
     * 设置当前题目的独立选项 JSON。
     */
    public void setOptionsJson(String optionsJson) {
        this.optionsJson = optionsJson;
    }

}
