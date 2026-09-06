-- ============================================================
-- Reading Question Group
--
-- 用来表示一组共享 instruction / question type 的 Reading 题目。
--
-- 例如：
--
-- Questions 14-18
-- Choose the correct heading for each paragraph.
--
-- 数据关系暂时为：
--
-- reading_passage
--      |
--      | 1 : N
--      ↓
-- question_group
--
-- 下一步我们才会让：
--
-- question_group
--      ↓
-- reading_question
-- ============================================================

CREATE TABLE question_group (

    -- Question Group 主键
                                id INTEGER PRIMARY KEY AUTOINCREMENT,

    -- 当前题组属于哪一篇 Passage
                                passage_id INTEGER NOT NULL,

    -- 当前题组的题型
    --
    -- 例如：
    -- MATCHING_HEADINGS
    -- MATCHING_FEATURES
    -- TRUE_FALSE_NOT_GIVEN
                                question_type TEXT NOT NULL,

    -- 整组题目的答题说明
    --
    -- 例如：
    -- Choose the correct heading for each paragraph
    -- from the list of headings below.
                                instruction TEXT,

    -- 是否允许重复使用同一个选项
    --
    -- SQLite 中：
    -- 0 = false
    -- 1 = true
    --
    -- 例如：
    -- NB You may use any letter more than once.
                                allow_option_reuse INTEGER NOT NULL DEFAULT 0,

    -- 外键：
    -- 当前 Question Group 属于哪一个 Reading Passage
                                FOREIGN KEY (passage_id)
                                    REFERENCES reading_passage(id)
);
