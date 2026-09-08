package com.duorou.ieltsbackend.reading.repository;

import com.duorou.ieltsbackend.reading.entity.ReadingPracticeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ReadingPracticeRecordRepository
 *
 * 负责操作 reading_practice_record 表。
 *
 * 因为继承 JpaRepository，
 * 所以我们会自动获得常用数据库操作：
 *
 * save(...)
 * findById(...)
 * findAll()
 * delete(...)
 *
 * 当前阶段先不写自定义 SQL。
 */
public interface ReadingPracticeRecordRepository
        extends JpaRepository<ReadingPracticeRecord, Long> {
}
