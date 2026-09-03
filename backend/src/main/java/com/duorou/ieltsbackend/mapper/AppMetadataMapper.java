package com.duorou.ieltsbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * app_metadata 表的数据访问接口。
 *
 * Mapper 可以理解为：
 *
 * Java 代码
 *    ↓
 * Mapper
 *    ↓
 * SQL
 *    ↓
 * Database
 *
 * 我们不需要自己编写 JDBC Connection、PreparedStatement 等代码，
 * MyBatis 会根据这个接口帮我们完成数据库访问。
 */
@Mapper
public interface AppMetadataMapper {

    /**
     * 查询 app_metadata 表中有多少条记录。
     *
     * @Select 表示：
     * 当前方法执行下面这条 SQL。
     *
     * SQL：
     * SELECT COUNT(*) FROM app_metadata
     *
     * COUNT(*) 返回整数，所以 Java 方法返回 int。
     */
    @Select("SELECT COUNT(*) FROM app_metadata")
    int count();
}
