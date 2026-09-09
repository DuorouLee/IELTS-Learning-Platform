package com.duorou.ieltsbackend.reading.exception;

/**
 * Reading Test 不存在时抛出的业务异常。
 *
 * 例如：
 *
 * POST /api/reading/tests/999999/submit
 *
 * 如果 testId = 999999 不存在，
 * 就抛出这个异常。
 */
public class ReadingTestNotFoundException extends RuntimeException {

    /**
     * @param testId 不存在的 Reading Test ID
     */
    public ReadingTestNotFoundException(Long testId) {
        super("Reading test not found: " + testId);
    }
}
