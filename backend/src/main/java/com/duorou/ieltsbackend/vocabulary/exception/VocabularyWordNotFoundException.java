package com.duorou.ieltsbackend.vocabulary.exception;

/**
 * 当根据 id 查询 Vocabulary Word，
 * 但数据库中不存在对应记录时，
 * 抛出这个异常。
 */
public class VocabularyWordNotFoundException extends RuntimeException {

    /**
     * @param id 查询不到的 Vocabulary Word id
     */
    public VocabularyWordNotFoundException(Long id) {
        super("Vocabulary word not found: " + id);
    }
}
