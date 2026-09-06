package com.duorou.ieltsbackend.reading.entity;

import jakarta.persistence.*;

/**
 * QuestionOption 表示一个题组中的可选答案。
 *
 * 例如 MATCHING_HEADINGS：
 *
 * i    The beginning of the project
 * ii   Problems with the original design
 *
 * 每一个选项都属于某一个 QuestionGroup。
 */
@Entity
@Table(name = "question_option")
public class QuestionOption {

    /**
     * 数据库主键。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 当前选项属于哪个 QuestionGroup。
     *
     * 对应数据库：
     * question_option.question_group_id
     */
    @ManyToOne
    @JoinColumn(name = "question_group_id", nullable = false)
    private QuestionGroup questionGroup;

    /**
     * 选项真正用于保存答案的值。
     *
     * 例如：
     * i
     * ii
     * A
     * B
     */
    @Column(name = "option_value", nullable = false)
    private String optionValue;

    /**
     * 展示给用户看的选项文字。
     *
     * 例如：
     * Problems with the original design
     */
    @Column(name = "option_text", nullable = false)
    private String optionText;

    /**
     * 控制选项显示顺序。
     *
     * 例如：
     * 1 -> i
     * 2 -> ii
     * 3 -> iii
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public QuestionOption() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public QuestionGroup getQuestionGroup() {
        return questionGroup;
    }

    public void setQuestionGroup(QuestionGroup questionGroup) {
        this.questionGroup = questionGroup;
    }

    public String getOptionValue() {
        return optionValue;
    }

    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
