package com.duorou.ieltsbackend.reading.repository;

import com.duorou.ieltsbackend.reading.entity.ReadingTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

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
 * Spring Data JPA 会自动提供：
 *
 * save()
 * findById()
 * findAll()
 * delete()
 * deleteById()
 *
 * 等常用数据库操作。
 */
public interface ReadingTestRepository
        extends JpaRepository<ReadingTest, Long> {

    /**
     * 判断指定 externalId 的 Reading Test 是否存在。
     *
     * 例如：
     *
     * yasige-4202607160914251713
     */
    boolean existsByExternalId(String externalId);

    /**
     * 根据 externalId 查询 Reading Test。
     *
     * 为什么返回 Optional？
     *
     * 因为数据库中可能不存在这条记录。
     *
     * Optional 可以让我们显式处理：
     *
     * 找到
     * 或
     * 没找到
     *
     * 两种情况。
     */
    Optional<ReadingTest> findByExternalId(String externalId);
}
