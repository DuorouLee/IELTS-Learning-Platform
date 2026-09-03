package com.duorou.ieltsbackend.reading.repository;

import com.duorou.ieltsbackend.reading.entity.ReadingPassage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ReadingPassageRepository
 * <p>
 * 负责操作 reading_passage 表。
 */
public interface ReadingPassageRepository
        extends JpaRepository<ReadingPassage, Long> {
    /**
     * 查询某一个 ReadingTest 下的所有 Passage。
     * <p>
     * Spring Data JPA 会根据方法名自动生成查询。
     */
    List<ReadingPassage> findByReadingTestId(Long testId);
}

