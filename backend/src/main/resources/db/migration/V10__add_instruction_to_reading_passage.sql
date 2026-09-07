/*
  给 reading_passage 表新增 instruction 字段。

  instruction 用来保存整篇 Reading Passage 共用的题目说明。

  例如：

  You should spend about 20 minutes on Questions 1-13,
  which are based on Reading Passage 1 on the following pages.

  这样以后前端可以把 instruction 放在
  Reading Passage 1 标题下面，
  而不是混在正文 content 里面。
*/
ALTER TABLE reading_passage
    ADD COLUMN instruction TEXT;
