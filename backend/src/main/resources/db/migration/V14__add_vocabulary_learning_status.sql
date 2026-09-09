-- 给 vocabulary_word 增加学习状态。
--
-- LEARNING：
-- 当前仍在学习。
--
-- MASTERED：
-- 用户认为已经掌握。
--
-- 新创建的单词默认都是 LEARNING。
ALTER TABLE vocabulary_word
    ADD COLUMN learning_status TEXT NOT NULL DEFAULT 'LEARNING';
