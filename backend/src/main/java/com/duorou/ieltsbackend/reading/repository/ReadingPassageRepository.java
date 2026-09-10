package com.duorou.ieltsbackend.reading.repository;

import com.duorou.ieltsbackend.reading.entity.ReadingPassage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ReadingPassageRepository
 *
 * 负责操作 reading_passage 表。
 */
public interface ReadingPassageRepository
        extends JpaRepository<ReadingPassage, Long> {

    /**
     * 查询某一个 ReadingTest 下的所有 Passage。
     */
    List<ReadingPassage> findByReadingTestId(Long testId);

    /**
     * 删除某一个 ReadingTest 下的全部 Passage。
     *
     * 调用这个方法之前，
     * 必须确保 Passage 下的 QuestionGroup 和 ReadingQuestion
     * 都已经被删除。
     */
    void deleteByReadingTestId(Long testId);
}
