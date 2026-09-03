-- ============================================================
-- IELTS Reading 模块
--
-- 数据关系：
--
-- reading_test
--      |
--      | 1 : N
--      ↓
-- reading_passage
--      |
--      | 1 : N
--      ↓
-- reading_question
--
-- ============================================================


-- ============================================================
-- 1. Reading Test
-- 表示一整套 IELTS Reading 测试
--
-- 例如：
-- Cambridge IELTS 18 - Test 1
-- ============================================================

CREATE TABLE reading_test (
                              id INTEGER PRIMARY KEY AUTOINCREMENT,

    -- 测试名称
    -- 例如：Cambridge IELTS 18 Test 1
                              title TEXT NOT NULL,

    -- 题目来源
    -- 例如：Cambridge IELTS 18
                              source TEXT,

    -- 创建时间
                              created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================
-- 2. Reading Passage
-- 表示 Reading Test 中的一篇文章
--
-- 一套 IELTS Reading 通常有：
-- Passage 1
-- Passage 2
-- Passage 3
-- ============================================================

CREATE TABLE reading_passage (
                                 id INTEGER PRIMARY KEY AUTOINCREMENT,

    -- 外键：
    -- 当前 Passage 属于哪个 Reading Test
                                 test_id INTEGER NOT NULL,

    -- Passage 编号
    -- 例如：1 / 2 / 3
                                 passage_number INTEGER NOT NULL,

    -- 文章标题
                                 title TEXT,

    -- Reading 文章正文
                                 content TEXT NOT NULL,

    -- 建立和 reading_test 的关系
                                 FOREIGN KEY (test_id)
                                     REFERENCES reading_test(id)
);


-- ============================================================
-- 3. Reading Question
-- 表示 Passage 中的一道题
-- ============================================================

CREATE TABLE reading_question (
                                  id INTEGER PRIMARY KEY AUTOINCREMENT,

    -- 外键：
    -- 当前 Question 属于哪一篇 Passage
                                  passage_id INTEGER NOT NULL,

    -- 题号
    -- 例如：1、2、3...
                                  question_number INTEGER NOT NULL,

    -- IELTS Reading 题型
    --
    -- 第一版我们直接使用字符串保存。
    --
    -- 例如：
    -- TRUE_FALSE_NOT_GIVEN
    -- YES_NO_NOT_GIVEN
    -- MULTIPLE_CHOICE
    -- MATCHING_HEADINGS
    -- FILL_IN_THE_BLANK
                                  question_type TEXT NOT NULL,

    -- 题目内容
                                  question_text TEXT NOT NULL,

    -- 正确答案
                                  correct_answer TEXT NOT NULL,

    -- 答案解析
    -- 后面可以用于学习模式
                                  explanation TEXT,

    -- 建立和 reading_passage 的关系
                                  FOREIGN KEY (passage_id)
                                      REFERENCES reading_passage(id)
);
