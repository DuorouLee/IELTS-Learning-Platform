package com.duorou.ieltsbackend.reading.dto;

/**
 * ReadingSubmitResponse
 *
 * 用来返回一整套 Reading 提交后的判分结果。
 *
 * 这一版先只返回最核心的数据：
 *
 * 1. 总题数
 * 2. 正确题数
 * 3. 错误题数
 * 4. 正确率
 *
 * 后面做 Review 页面时，
 * 再单独增加每一道题的：
 *
 * - userAnswer
 * - correctAnswer
 * - isCorrect
 * - explanation
 *
 * 现在先不要一次做太多。
 */
public class ReadingSubmitResponse {

    /**
     * 当前 Reading Test 的总题数。
     *
     * 例如：
     * 13
     */
    private int totalQuestions;

    /**
     * 用户答对多少题。
     *
     * 例如：
     * 10
     */
    private int correctCount;

    /**
     * 用户答错多少题。
     *
     * 例如：
     * 3
     */
    private int incorrectCount;

    /**
     * 正确率。
     *
     * 例如：
     *
     * 10 / 13
     * ↓
     * 76.92
     *
     * 当前先使用百分比，
     * 后面如果要做 IELTS Band Score，
     * 再单独设计转换逻辑。
     */
    private double percentage;

    /**
     * 构造方法。
     *
     * Service 完成判分以后，
     * 会创建这个 Response 返回给 Controller。
     */
    public ReadingSubmitResponse(
            int totalQuestions,
            int correctCount,
            int incorrectCount,
            double percentage
    ) {
        this.totalQuestions = totalQuestions;
        this.correctCount = correctCount;
        this.incorrectCount = incorrectCount;
        this.percentage = percentage;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getIncorrectCount() {
        return incorrectCount;
    }

    public double getPercentage() {
        return percentage;
    }
}
