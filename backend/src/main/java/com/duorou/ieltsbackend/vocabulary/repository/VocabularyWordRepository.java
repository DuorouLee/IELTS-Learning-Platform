package com.duorou.ieltsbackend.vocabulary.repository;

import com.duorou.ieltsbackend.vocabulary.entity.VocabularyWord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * VocabularyWordRepository
 *
 * 负责访问 vocabulary_word 数据表。
 *
 * 在当前项目架构中：
 *
 * Controller
 *     ↓
 * Service
 *     ↓
 * Repository
 *     ↓
 * Database
 *
 * Repository 这一层只负责数据库访问，
 * 不在这里编写业务逻辑。
 */
public interface VocabularyWordRepository
        extends JpaRepository<VocabularyWord, Long> {

    /**
     * 根据单词查询 VocabularyWord。
     *
     * Spring Data JPA 会根据方法名：
     *
     * findByWord
     *
     * 自动生成类似下面的 SQL：
     *
     * SELECT *
     * FROM vocabulary_word
     * WHERE word = ?
     *
     * Optional 的意思是：
     *
     * 这个单词可能存在，也可能不存在。
     */
    Optional<VocabularyWord> findByWord(String word);

    /**
     * 判断某个单词是否已经存在。
     *
     * 后面添加单词时可以利用它避免重复。
     *
     * Spring Data JPA 同样会自动生成查询，
     * 我们不需要自己写 SQL。
     */
    boolean existsByWord(String word);
}
