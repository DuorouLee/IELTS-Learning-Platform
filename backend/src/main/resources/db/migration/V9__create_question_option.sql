-- ============================================================
-- Question Option
--
-- 用来保存 Reading 题组中的可选答案。
--
-- 例如 MATCHING_HEADINGS：
--
-- i    The importance of...
-- ii   A new approach...
-- iii  Problems caused by...
--
-- 这些选项属于某一个 QuestionGroup。
-- ============================================================

CREATE TABLE question_option (

    -- 主键
                                 id INTEGER PRIMARY KEY AUTOINCREMENT,

    -- 当前选项属于哪一个题组
                                 question_group_id INTEGER NOT NULL,

    -- 用户真正选择 / 提交的值
    --
    -- 例如：
    -- i
    -- ii
    -- A
    -- B
                                 option_value TEXT NOT NULL,

    -- 展示给用户看的文字
    --
    -- 例如：
    -- The importance of early education
                                 option_text TEXT NOT NULL,

    -- 控制选项显示顺序
    --
    -- 例如：
    -- 1 -> i
    -- 2 -> ii
    -- 3 -> iii
                                 display_order INTEGER NOT NULL,

    -- 外键：
    -- 当前选项必须属于一个 question_group
                                 FOREIGN KEY (question_group_id)
                                     REFERENCES question_group(id)
);
