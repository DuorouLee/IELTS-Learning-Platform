package com.duorou.ieltsbackend.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.duorou.ieltsbackend.reading.exception.ReadingTestNotFoundException;

import java.util.Map;

/**
 * 全局异常处理器。
 *
 * 作用：
 * 把 Service 层抛出的异常，
 * 转换成更合理的 HTTP Response。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 IllegalArgumentException。
     *
     * 当前 ReadingSubmissionService 在找不到 testId 时会抛：
     *
     * IllegalArgumentException(
     *     "Reading test not found: " + testId
     * )
     *
     * 这里把它转换成：
     *
     * HTTP 404 Not Found
     */
    /**
     * Reading Test 不存在时返回 HTTP 404。
     */
    @ExceptionHandler(ReadingTestNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleReadingTestNotFoundException(
            ReadingTestNotFoundException exception
    ) {

        Map<String, String> body = Map.of(
                "message",
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }
}
