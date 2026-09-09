CREATE TABLE vocabulary_word (
                                 id INTEGER PRIMARY KEY AUTOINCREMENT,

    -- 单词本身，例如：
    -- abandon
                                 word TEXT NOT NULL UNIQUE,

    -- 中文释义或主要含义。
    -- 先使用 TEXT，后面如果需要支持多义词，
    -- 再单独设计 meaning 表，不在第一步过度设计。
                                 meaning TEXT NOT NULL,

    -- 示例句。
    -- 允许为空，因为有些单词刚导入时可能还没有例句。
                                 example_sentence TEXT,

    -- 单词创建时间。
    --
    -- SQLite 没有真正的 DATETIME 类型，
    -- CURRENT_TIMESTAMP 会自动保存当前时间。
                                 created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
