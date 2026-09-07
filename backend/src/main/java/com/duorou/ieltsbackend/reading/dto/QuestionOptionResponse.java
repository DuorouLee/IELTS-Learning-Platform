package com.duorou.ieltsbackend.reading.dto;

/**
 * QuestionOptionResponse
 *
 * 用于 API 返回一个题组选项。
 *
 * 例如 MATCHING_HEADINGS：
 *
 * i    A chance discovery
 * ii   Religious objections
 */
public class QuestionOptionResponse {

    private Long id;

    /**
     * 真正用于答题和判分的值。
     *
     * 例如：
     * i
     * ii
     * A
     * B
     */
    private String optionValue;

    /**
     * 展示给用户看的文字。
     */
    private String optionText;

    /**
     * 控制前端显示顺序。
     */
    private Integer displayOrder;

    public QuestionOptionResponse(
            Long id,
            String optionValue,
            String optionText,
            Integer displayOrder
    ) {
        this.id = id;
        this.optionValue = optionValue;
        this.optionText = optionText;
        this.displayOrder = displayOrder;
    }

    public Long getId() {
        return id;
    }

    public String getOptionValue() {
        return optionValue;
    }

    public String getOptionText() {
        return optionText;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }
}
