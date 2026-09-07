package com.duorou.ieltsbackend.reading.importer.dto;

/**
 * QuestionOptionImportDto
 *
 * 用来接收 JSON 中的题组选项。
 *
 * 例如：
 *
 * {
 *   "optionValue": "i",
 *   "optionText": "The beginning of the project",
 *   "displayOrder": 1
 * }
 */
public class QuestionOptionImportDto {

    /**
     * 真正用于答案匹配的值。
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
     * 选项显示顺序。
     */
    private Integer displayOrder;

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
