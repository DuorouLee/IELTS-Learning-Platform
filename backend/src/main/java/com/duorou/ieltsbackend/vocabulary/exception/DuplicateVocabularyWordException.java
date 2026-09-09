package com.duorou.ieltsbackend.vocabulary.exception;

/**
 * 当用户尝试创建一个已经存在的单词时，
 * 抛出这个业务异常。
 */
public class DuplicateVocabularyWordException extends RuntimeException {

    public DuplicateVocabularyWordException(String word) {
        super("Vocabulary word already exists: " + word);
    }
}
