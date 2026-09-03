package com.duorou.ieltsbackend.reading.repository;

import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ReadingTestRepository
 *
 * 作用：
 * 负责操作 reading_test 表。
 *
 * JpaRepository<ReadingTest, Long>
 *
 * 第一个泛型 ReadingTest：
 * 表示这个 Repository 操作的是 ReadingTest 实体。
 *
 * 第二个泛型 Long：
 * 表示 ReadingTest 的主键 id 类型是 Long。
 *
 * 继承 JpaRepository 后，
 * Spring Data JPA 会自动为我们提供常用数据库操作，例如：
 *
 * save()      保存数据
 * findById()  根据 id 查询
 * findAll()   查询全部
 * deleteById() 根据 id 删除
 *
 * 这些基础 SQL 不需要我们自己写。
 */
public interface ReadingTestRepository
        extends JpaRepository<ReadingTest, Long> {
}
