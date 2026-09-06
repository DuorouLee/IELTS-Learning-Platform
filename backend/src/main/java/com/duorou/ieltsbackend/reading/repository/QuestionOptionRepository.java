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
}
