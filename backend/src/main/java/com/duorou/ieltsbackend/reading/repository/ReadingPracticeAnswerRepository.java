package com.duorou.ieltsbackend.reading.repository;

import com.duorou.ieltsbackend.reading.entity.ReadingPracticeAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ReadingPracticeAnswer Repository。
 *
 * 用来保存和查询某一次 Reading Practice
 * 中每一道题的历史作答结果。
 */
public interface ReadingPracticeAnswerRepository
        extends JpaRepository<ReadingPracticeAnswer, Long> {

    /**
     * 根据 Practice Record ID，
     * 查询这一次练习中的所有题目答案。
     *
     * 并按照 IELTS 题号从小到大排序。
     *
     * Spring Data JPA 会根据方法名自动生成查询。
     */
    List<ReadingPracticeAnswer>
    findByPracticeRecordIdOrderByQuestionNumberAsc(Long practiceRecordId);
}
