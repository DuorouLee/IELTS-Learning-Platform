package com.duorou.ieltsbackend.reading.repository;

import com.duorou.ieltsbackend.reading.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * QuestionOptionRepository
 *
 * 用于操作 question_option 表。
 */
public interface QuestionOptionRepository
        extends JpaRepository<QuestionOption, Long> {

    /**
     * 删除某一整套 Reading Test 下所有 QuestionGroup 的选项。
     *
     * 对象关系：
     *
     * QuestionOption
     *      ↓ questionGroup
     * QuestionGroup
     *      ↓ readingPassage
     * ReadingPassage
     *      ↓ readingTest
     * ReadingTest
     *      ↓ id
     *
     * Spring Data JPA 会根据方法名自动生成对应删除语句。
     */
    void deleteByQuestionGroupReadingPassageReadingTestId(Long testId);
}
