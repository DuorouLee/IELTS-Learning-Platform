package com.duorou.ieltsbackend.reading.dto;

import java.util.Map;

/**
 * ReadingSubmitRequest
 *
 * 用来接收前端提交的一整套 Reading 答案。
 *
 * 前端现在 answers 的结构是：
 *
 * Record<number, string>
 *
 * 例如：
 *
 * {
 *   27: "viii",
 *   28: "iv",
 *   35: "D"
 * }
 *
 * 发送到后端以后，对应 JSON：
 *
 * {
 *   "answers": {
 *     "27": "viii",
 *     "28": "iv",
 *     "35": "D"
 *   }
 * }
 *
 * 这里使用：
 *
 * Map<Long, String>
 *
 * key：
 * ReadingQuestion 的 id
 *
 * value：
 * 用户提交的答案
 */
public class ReadingSubmitRequest {

    /**
     * 用户提交的答案。
     *
     * 例如：
     *
     * questionId = 27
     * answer = "viii"
     *
     * 最终：
     *
     * answers.get(27L) -> "viii"
     */
    private Map<Long, String> answers;

    /**
     * Spring / Jackson 需要通过 setter
     * 把前端 JSON 数据写入这个对象。
     */
    public void setAnswers(Map<Long, String> answers) {
        this.answers = answers;
    }

    /**
     * Service 后面通过 getter
     * 读取用户提交的答案。
     */
    public Map<Long, String> getAnswers() {
        return answers;
    }
}
