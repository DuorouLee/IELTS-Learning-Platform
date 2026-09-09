package com.duorou.ieltsbackend.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.duorou.ieltsbackend.reading.exception.ReadingTestNotFoundException;
import com.duorou.ieltsbackend.vocabulary.exception.DuplicateVocabularyWordException;
import com.duorou.ieltsbackend.vocabulary.exception.VocabularyWordNotFoundException;

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

    /**
     * Vocabulary 单词重复时返回 HTTP 400 Bad Request。
     *
     * 例如：
     *
     * POST /api/vocabulary/words
     *
     * 如果 abandon 已经存在，
     * Service 会抛出 DuplicateVocabularyWordException。
     *
     * 这里负责把 Java 异常转换成 HTTP Response。
     */
    @ExceptionHandler(DuplicateVocabularyWordException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateVocabularyWordException(
            DuplicateVocabularyWordException exception
    ) {

        Map<String, String> body = Map.of(
                "message",
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    /**
     * 当查询不到 Vocabulary Word 时，
     * 返回 HTTP 404 Not Found。
     */
    @ExceptionHandler(VocabularyWordNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleVocabularyWordNotFoundException(
            VocabularyWordNotFoundException exception
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
