-- ============================================================
-- Add group_id to reading_question
--
-- 目标：
-- 让 ReadingQuestion 可以属于某一个 QuestionGroup。
--
-- 新结构：
--
-- reading_passage
--      ↓
-- question_group
--      ↓
-- reading_question
--
-- 注意：
-- 目前暂时保留 reading_question.passsage_id，
-- 因为旧数据还依赖它。
-- ============================================================

ALTER TABLE reading_question
    ADD COLUMN group_id INTEGER;

-- SQLite 中 ALTER TABLE 添加外键约束比较受限，
-- 所以这里先只新增字段。
--
-- 后面我们再通过迁移或重建表的方式，
-- 正式加入 FOREIGN KEY 约束。
