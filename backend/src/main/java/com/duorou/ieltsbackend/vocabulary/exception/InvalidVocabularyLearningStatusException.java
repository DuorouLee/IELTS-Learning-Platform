package com.duorou.ieltsbackend.vocabulary.exception;

/**
 * 当 Vocabulary 学习状态不是系统支持的值时，
 * 抛出这个业务异常。
 *
 * 当前只允许：
 *
 * LEARNING
 * MASTERED
 */
public class InvalidVocabularyLearningStatusException
        extends RuntimeException {

    /**
     * @param learningStatus 前端传来的非法状态
     */
    public InvalidVocabularyLearningStatusException(
            String learningStatus
    ) {
        super(
                "Invalid vocabulary learning status: "
                        + learningStatus
        );
    }
}
