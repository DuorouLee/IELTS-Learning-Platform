package com.duorou.ieltsbackend.reading.repository;

import com.duorou.ieltsbackend.reading.entity.ReadingPracticeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Reading Practice History Repository。
 */
public interface ReadingPracticeRecordRepository
        extends JpaRepository<ReadingPracticeRecord, Long> {

    /**
     * 查询所有 Reading Practice History，
     * 并按照提交时间从新到旧排序。
     *
     * Spring Data JPA 会根据方法名自动生成查询。
     */
    List<ReadingPracticeRecord> findAllByOrderBySubmittedAtDesc();
}
