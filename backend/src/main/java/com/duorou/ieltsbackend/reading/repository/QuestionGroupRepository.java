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
     * 查询某一篇 ReadingPassage 下的所有题组。
     *
     * 方法名中的：
     *
     * ReadingPassage_Id
     *
     * 会被 Spring Data JPA 自动解析为：
     *
     * QuestionGroup.readingPassage.id
     *
     * 也就是类似 SQL：
     *
     * SELECT *
     * FROM question_group
     * WHERE passage_id = ?
     *
     * @param passageId ReadingPassage 的数据库 ID
     * @return 当前 Passage 下的所有 QuestionGroup
     */
    List<QuestionGroup> findByReadingPassage_Id(Long passageId);
}
