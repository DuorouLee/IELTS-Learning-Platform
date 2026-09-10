-- 保存题目对应的原文高亮信息。
-- 数据来源是外部 Reading 数据中的 articleSourceHighlight。
ALTER TABLE reading_question
    ADD COLUMN answer_highlight_json TEXT;
