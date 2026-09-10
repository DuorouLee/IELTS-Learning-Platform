package com.duorou.ieltsbackend.reading.repository;

import com.duorou.ieltsbackend.reading.entity.QuestionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * QuestionGroupRepository
 *
 * 负责 question_group 表的数据库操作。
 */
public interface QuestionGroupRepository
        extends JpaRepository<QuestionGroup, Long> {

    /**
     * 查询某一个 Passage 下的全部 QuestionGroup。
     */
    List<QuestionGroup> findByReadingPassage_Id(Long passageId);

    /**
     * 删除某一整套 Reading Test 下的全部 QuestionGroup。
     *
     * 删除 QuestionGroup 之前，
     * 必须先删除它下面的 QuestionOption。
     */
    void deleteByReadingPassageReadingTestId(Long testId);
}
