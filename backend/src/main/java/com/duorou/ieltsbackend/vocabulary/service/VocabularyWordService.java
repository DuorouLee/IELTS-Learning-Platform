package com.duorou.ieltsbackend.vocabulary.service;

import com.duorou.ieltsbackend.vocabulary.entity.VocabularyWord;
import com.duorou.ieltsbackend.vocabulary.repository.VocabularyWordRepository;
import org.springframework.stereotype.Service;
import com.duorou.ieltsbackend.vocabulary.exception.DuplicateVocabularyWordException;

import java.util.List;

/**
 * VocabularyWordService
 *
 * Service 层负责 Vocabulary 的业务逻辑。
 *
 * 当前调用链：
 *
 * Controller
 *     ↓
 * VocabularyWordService
 *     ↓
 * VocabularyWordRepository
 *     ↓
 * vocabulary_word
 *
 * 这一阶段先实现最基础的“查询全部单词”功能。
 */
@Service
public class VocabularyWordService {

    /**
     * VocabularyWordRepository
     *
     * Service 不直接操作数据库，
     * 而是通过 Repository 完成数据库访问。
     */
    private final VocabularyWordRepository vocabularyWordRepository;

    /**
     * 构造器注入。
     *
     * Spring 会自动找到 VocabularyWordRepository，
     * 并把它传给这个 Service。
     *
     * 这种写法比字段上直接写 @Autowired 更清晰，
     * 也更方便后面写自动化测试。
     */
    public VocabularyWordService(
            VocabularyWordRepository vocabularyWordRepository
    ) {
        this.vocabularyWordRepository = vocabularyWordRepository;
    }

    /**
     * 查询所有 Vocabulary Word。
     *
     * JpaRepository 已经提供 findAll()，
     * 所以这里直接调用即可。
     *
     * @return 数据库中的全部单词
     */
    public List<VocabularyWord> getAllWords() {
        return vocabularyWordRepository.findAll();
    }

    /**
     * 创建一个新的 Vocabulary Word。
     *
     * 保存之前先检查数据库中是否已经存在相同单词。
     *
     * 如果已经存在：
     * 抛出 DuplicateVocabularyWordException。
     *
     * 如果不存在：
     * 才真正调用 save()。
     */
    public VocabularyWord createWord(VocabularyWord vocabularyWord) {

        /**
         * existsByWord(...) 是 Repository 中已经定义的方法。
         *
         * Spring Data JPA 会自动生成对应的数据库查询。
         */
        if (vocabularyWordRepository.existsByWord(vocabularyWord.getWord())) {
            throw new DuplicateVocabularyWordException(
                    vocabularyWord.getWord()
            );
        }

        return vocabularyWordRepository.save(vocabularyWord);
    }
}
