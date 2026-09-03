package com.duorou.ieltsbackend.reading.repository;

import com.duorou.ieltsbackend.reading.entity.ReadingQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


/**
 * ReadingQuestionRepository
 * <p>
 * 负责操作 reading_question 表。
 */
public interface ReadingQuestionRepository
        extends JpaRepository<ReadingQuestion, Long> {

    /**
     * 根据 passageId 查询这个 Passage 下的全部题目。
     * <p>
     * Spring Data JPA 会根据方法名自动生成查询逻辑。
     */
    List<ReadingQuestion> findByReadingPassageId(Long passageId);

    /**
     * 根据 ReadingTest id 查询这个 Test 下的所有 Question。
     *
     * 查询关系：
     *
     * ReadingQuestion
     *      ↓
     * readingPassage
     *      ↓
     * readingTest
     *      ↓
     * id
     */
    List<ReadingQuestion> findByReadingPassageReadingTestId(Long testId);
}
