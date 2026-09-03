package com.duorou.ieltsbackend.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * AppMetadataMapper 的数据库集成测试。
 *
 * @SpringBootTest 会启动 Spring Boot 测试环境，
 * 因此：
 *
 * Spring Boot
 *    ↓
 * DataSource
 *    ↓
 * SQLite
 *    ↓
 * Flyway
 *    ↓
 * MyBatis
 *
 * 都会真正参与这次测试。
 */
@SpringBootTest
class AppMetadataMapperTest {

    /**
     * Spring 自动把 AppMetadataMapper 注入进来。
     *
     * 我们不需要：
     *
     * new AppMetadataMapper()
     *
     * 实际上接口也不能直接 new。
     *
     * MyBatis 会生成实现对象，Spring 再把这个对象交给这里。
     */
    @Autowired
    private AppMetadataMapper appMetadataMapper;

    @Test
    void shouldCountMetadataRecords() {

        // 调用 Mapper。
        // 实际执行：
        // SELECT COUNT(*) FROM app_metadata
        int count = appMetadataMapper.count();

        /*
         * 我们目前还没有往 app_metadata 插入任何数据，
         * 所以理论上应该有 0 条记录。
         */
        assertEquals(0, count);
    }
}
