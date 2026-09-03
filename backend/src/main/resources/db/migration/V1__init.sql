-- V1：初始化数据库
-- 这个版本只创建一张元数据表，用来验证 Flyway + SQLite 是否能正常工作。

-- V1      = 第 1 个数据库版本
-- __      = 两个下划线
-- init    = 这次迁移的说明
-- .sql    = SQL 文件
CREATE TABLE app_metadata (
                              id INTEGER PRIMARY KEY AUTOINCREMENT,
                              meta_key TEXT NOT NULL UNIQUE,
                              meta_value TEXT,
                              created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
