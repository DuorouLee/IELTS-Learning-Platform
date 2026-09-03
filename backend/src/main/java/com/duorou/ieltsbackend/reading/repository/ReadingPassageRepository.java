package com.duorou.ieltsbackend.reading.repository;

import com.duorou.ieltsbackend.reading.entity.ReadingPassage;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ReadingPassageRepository
 *
 * 负责操作 reading_passage 表。
 */
public interface ReadingPassageRepository
        extends JpaRepository<ReadingPassage, Long> {
}
