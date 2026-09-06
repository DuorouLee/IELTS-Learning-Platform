package com.duorou.ieltsbackend.reading.repository;

import com.duorou.ieltsbackend.reading.entity.QuestionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * QuestionGroupRepository
 *
 * 负责操作数据库中的 question_group 表。
 *
 * 因为继承 JpaRepository，
 * Spring Data JPA 会自动提供常用数据库操作，例如：
 *
 * save()       保存或更新 QuestionGroup
 * findById()   根据 id 查询
 * findAll()    查询全部
 * deleteById() 根据 id 删除
 *
 * 所以目前不需要自己写 SQL。
 */
public interface QuestionGroupRepository
        extends JpaRepository<QuestionGroup, Long> {
}
