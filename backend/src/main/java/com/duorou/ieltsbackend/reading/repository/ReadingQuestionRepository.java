package com.duorou.ieltsbackend.reading.repository;

import com.duorou.ieltsbackend.reading.entity.ReadingQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ReadingQuestionRepository
 *
 * 负责操作 reading_question 表。
 */
public interface ReadingQuestionRepository
        extends JpaRepository<ReadingQuestion, Long> {

    /**
     * 查询某一个 Passage 下的全部题目。
     */
    List<ReadingQuestion> findByReadingPassageId(Long passageId);

    /**
     * 查询某一整套 Reading Test 下的所有 Question。
     *
     * Spring Data JPA 会自动按照下面的对象关系解析：
     *
     * ReadingQuestion
     *      ↓ readingPassage
     * ReadingPassage
     *      ↓ readingTest
     * ReadingTest
     *      ↓ id
     */
    List<ReadingQuestion> findByReadingPassageReadingTestId(Long testId);

    /**
     * 删除某一整套 Reading Test 下的全部 Question。
     *
     * 后续重新导入真实题库时使用。
     *
     * 注意：
     * 这里只负责 Question，
     * 不会删除 Passage / Group / Test。
     */
    void deleteByReadingPassageReadingTestId(Long testId);
}
