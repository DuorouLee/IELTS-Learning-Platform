package com.duorou.ieltsbackend.reading.entity;

import jakarta.persistence.*;

/**
 * ReadingPracticeAnswer
 *
 * 保存某一次 Reading Practice 中，
 * 某一道题的实际作答结果。
 *
 * 例如：
 *
 * Practice Record #10
 *      ↓
 * Question 27
 *      ↓
 * userAnswer = "viii"
 * correctAnswer = "vii"
 * correct = false
 *
 * 这样以后用户查看某一次历史记录时，
 * 就可以看到当时每一道题具体答了什么。
 */
@Entity
@Table(name = "reading_practice_answer")
public class ReadingPracticeAnswer {

    /**
     * 主键。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 这道答案属于哪一次 Reading Practice。
     *
     * 多道答案可以属于同一条 Practice Record，
     * 所以这里是 Many-to-One。
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "practice_record_id", nullable = false)
    private ReadingPracticeRecord practiceRecord;

    /**
     * 原始 Reading Question ID。
     *
     * 当前先直接保存 questionId，
     * 方便以后找到对应题目。
     */
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    /**
     * IELTS 原始题号。
     *
     * 例如：
     * 1、2、27、35
     */
    @Column(name = "question_number", nullable = false)
    private Integer questionNumber;

    /**
     * 用户当时提交的答案。
     *
     * 未作答时允许为 null。
     */
    @Column(name = "user_answer")
    private String userAnswer;

    /**
     * 本次提交时对应的正确答案。
     *
     * 这里保存一份快照，
     * 防止未来题库答案修改后，
     * 历史 Review 内容发生变化。
     */
    @Column(name = "correct_answer", nullable = false)
    private String correctAnswer;

    /**
     * 当前题是否回答正确。
     */
    @Column(name = "correct", nullable = false)
    private boolean correct;


    public Long getId() {
        return id;
    }

    public ReadingPracticeRecord getPracticeRecord() {
        return practiceRecord;
    }

    public void setPracticeRecord(
            ReadingPracticeRecord practiceRecord
    ) {
        this.practiceRecord = practiceRecord;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(Integer questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}
