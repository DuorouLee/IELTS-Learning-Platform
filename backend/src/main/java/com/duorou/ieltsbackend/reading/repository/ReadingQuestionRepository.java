package com.duorou.ieltsbackend.reading.repository;

import com.duorou.ieltsbackend.reading.entity.ReadingQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ReadingQuestionRepository
 *
 * 负责操作 reading_question 表。
 */
public interface ReadingQuestionRepository
        extends JpaRepository<ReadingQuestion, Long> {
}
