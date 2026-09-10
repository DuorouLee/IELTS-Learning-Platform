-- 给 reading_question 增加“单题独立选项”字段。
--
-- 用途：
-- Multiple Choice 这种题型，每一道题都有自己独立的 A/B/C/D。
--
-- 示例保存内容：
-- ["Option A","Option B","Option C","Option D"]
--
-- 使用 TEXT 是因为 SQLite 本身没有独立的 JSON 类型。
ALTER TABLE reading_question
    ADD COLUMN options_json TEXT;
