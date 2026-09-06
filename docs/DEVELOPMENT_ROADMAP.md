# IELTS Learning Platform 开发路线

## 1. 项目目标

IELTS Learning Platform 是一个以 **Java + Spring Boot** 为核心后端的 IELTS 英语学习平台。

项目有两个主要目标：

1. 作为 Java 后端求职项目，练习完整的 Web 项目开发流程。
2. 作为个人 IELTS 学习工具，用于练习 Reading、记录错题和分析学习数据。

当前技术栈：

- Backend：Java / Spring Boot
- ORM：Spring Data JPA / Hibernate
- Database：SQLite
- Database Migration：Flyway
- Frontend：Vue 3 + TypeScript
- API：REST API
- Version Control：Git + GitHub

---

# 2. 整体架构

当前项目采用典型的前后端分离结构：

```text
Vue Frontend
      ↓
REST API
      ↓
Controller
      ↓
Service
      ↓
Repository
      ↓
Database
```

Spring Boot 后端内部继续按照分层架构组织代码：

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Entity
    ↓
SQLite
```

其中：

- Controller：负责接收 HTTP 请求和返回 HTTP 响应。
- Service：负责业务逻辑。
- Repository：负责数据库访问。
- Entity：负责 Java 对象和数据库表之间的映射。
- SQLite：保存 IELTS 题库和未来的学习数据。

---

# 3. 当前 Reading 数据模型

Reading 模块目前采用以下核心关系：

```text
ReadingTest
    ↓
ReadingPassage
    ↓
ReadingQuestion
```

含义：

```text
一套 Reading Test
    ↓
包含多个 Passage
    ↓
每个 Passage 包含多道 Question
```

例如：

```text
Cambridge IELTS 18 Test 1
│
├── Passage 1
│   ├── Question 1
│   ├── Question 2
│   └── Question 3
│
├── Passage 2
│   └── ...
│
└── Passage 3
    └── ...
```

---

# 4. 当前已经完成的内容

## 阶段 1：Spring Boot 后端基础

已完成：

- 创建 Spring Boot 项目。
- 配置 SQLite。
- 配置 Spring Data JPA。
- 配置 Flyway。
- Spring Boot 可以正常启动。
- 数据库可以正常创建和访问。

状态：

```text
完成
```

---

## 阶段 2：ReadingTest

已经建立：

```text
ReadingTest
```

并完成对应的：

```text
Entity
Repository
Service
Controller
```

可以通过 REST API 查询 Reading Test 数据。

状态：

```text
完成
```

---

## 阶段 3：ReadingPassage

已经建立：

```text
ReadingPassage
```

并建立：

```text
ReadingTest
    ↓
ReadingPassage
```

之间的数据库关系。

状态：

```text
完成
```

---

## 阶段 4：ReadingQuestion

已经建立：

```text
ReadingQuestion
```

并建立：

```text
ReadingPassage
    ↓
ReadingQuestion
```

之间的数据库关系。

目前 Question 已包含基础字段，例如：

```text
questionNumber
questionType
questionText
correctAnswer
explanation
```

状态：

```text
完成基础版本
```

---

## 阶段 5：Reading 完整查询 API

目前已经完成完整 Reading Test 查询接口：

```http
GET /api/reading/tests/{id}/full
```

例如：

```http
GET /api/reading/tests/1/full
```

可以一次返回：

```text
ReadingTest
    ↓
Passages
    ↓
Questions
```

当前返回数据结构已经能够支持前端进行基础 Reading 页面展示。

状态：

```text
完成
```

---

# 5. 当前开发位置

当前已经完成完整的 Reading 数据链路：

```text
真实 Reading JSON
    ↓
ReadingImportService
    ↓
ReadingTest / ReadingPassage / ReadingQuestion
    ↓
SQLite
    ↓
Repository
    ↓
Service
    ↓
Controller
    ↓
REST API
    ↓
Vue
```

目前已经可以：

从 JSON 读取真实 Reading 数据。
使用 Jackson 转换为 Import DTO。
将 DTO 转换为 Entity。
保存到 SQLite。
防止同一个 externalId 重复导入。
通过 REST API 查询完整 Reading Test。
Vue 调用 Spring Boot API。
Vue 页面显示真实 Passage 和 Question。

当前第一套真实题库：

externalId: p1-high-01

A Brief History of Tea

Passage 1

Questions 1-13

当前阶段的重点已经从：

“能不能显示题目”

转变为：

“能不能真正完成 IELTS 答题”

---

# 6. Vue 前端

Vue 3 前端已经完成基础初始化，并已经与 Spring Boot 后端建立连接。

当前链路：

```text
Vue
 ↓
GET /api/reading/tests/{id}/full
 ↓
Spring Boot
 ↓
SQLite
```

当前已经建立：

frontend/src/

├── api/
│ └── reading.ts
│
├── views/
│ └── ReadingTestView.vue
│
├── router/
│ └── index.ts
│
├── App.vue
│
└── main.ts

当前页面已经可以显示：

Reading Test Title

Passage

Question 1
Question 2
...
Question 13

状态：

完成基础版本

下一步需要从“文本展示页面”升级成“真正的 IELTS 答题页面”。

---

# 7. 真实 IELTS Reading 题库

目前已经开始接入原 IELTS Practice 项目中的真实 Reading 数据。

原始题库格式：

```text
JavaScript 数据文件
    ↓
passage
questionGroups
answerKey

当前采用的处理方式：

原始 JS
    ↓
转换成项目内部 JSON
    ↓
ReadingImportDto
    ↓
ReadingImportService
    ↓
Entity
    ↓
SQLite
```

目前已经完成第一套真实 Reading 数据：

p1-high-01

A Brief History of Tea

Questions 1-13

当前已支持导入的真实题型包括：

MATCHING_HEADINGS
MATCHING_FEATURES

状态：

完成第一套真实题库导入

---

# 8. 当前阶段：Question 模型升级

当前 `ReadingQuestion` 基础字段：

```text
id
questionNumber
questionType
questionText
correctAnswer
explanation
```

这套结构已经足够完成基础导入，但还不足以完整支持真实 IELTS 交互题型。

例如 Matching Headings 还需要：

题组说明
可选项
是否允许重复选择
题组范围

因此下一阶段计划加入：

ReadingQuestionGroup
QuestionOption

目标结构：

ReadingPassage
↓
ReadingQuestionGroup
↓
ReadingQuestion
↓
QuestionOption

例如：

Questions 1-8
Choose the correct heading for each paragraph

Options:
i
ii
iii
iv
...

Questions:
Q1
Q2
...
Q8

这一阶段的目标是让数据库不仅保存“答案”，还能够保存“答题规则和题目选项”。

状态：

当前进行

---

# 9. JSON 题库导入系统

目前已经建立：

```text
ReadingImportDto
ReadingPassageImportDto
ReadingQuestionImportDto
ReadingImportService
```

已经实现：

JSON
↓
Jackson ObjectMapper
↓
Import DTO
↓
Entity
↓
SQLite

同时已经支持：

externalId

用于防止同一份 Reading 数据被重复导入。

例如：

p1-high-01

如果已经存在：

拒绝重复导入

后续导入系统还需要继续支持：

QuestionGroup
Options
Instructions
allowOptionReuse

并逐步导入更多真实 Reading 文件。

---

# 10. 下一阶段目标：Reading 答题系统

完成 QuestionGroup / Option 数据结构后，开始开发真正的答题功能。

第一阶段优先支持：

````text
MATCHING_HEADINGS
MATCHING_FEATURES

页面流程：

用户打开 Reading Test
        ↓
阅读 Passage
        ↓
查看 Question Group
        ↓
选择答案
        ↓
保存前端答题状态
        ↓
Submit

前端需要记录：

questionId
userAnswer

后端后续接收：

ReadingSubmission

并进行统一判分。

---

# 17. 当前执行顺序

目前严格按照以下顺序开发：

```text
1. Spring Boot 基础                         ✅

2. SQLite + Flyway                         ✅

3. ReadingTest                             ✅

4. ReadingPassage                          ✅

5. ReadingQuestion                         ✅

6. Reading 完整 REST API                   ✅

7. Vue 3 初始化                            ✅

8. Vue 调用 Reading API                    ✅

9. Reading 基础页面                        ✅

10. JSON 题库导入结构                      ✅

11. ReadingImportService                   ✅

12. JSON → DTO → Entity → SQLite           ✅

13. externalId 防重复导入                  ✅

14. 第一套真实 Reading 数据导入            ✅

15. MATCHING_HEADINGS 数据导入             ✅

16. MATCHING_FEATURES 数据导入             ✅

17. QuestionGroup 数据模型升级             ← 当前

18. QuestionOption 数据模型

19. Reading Matching Headings 答题 UI

20. Reading Matching Features 答题 UI

21. Reading 答案提交 API

22. Reading 后端判分

23. ReadingAttempt / ReadingAnswer

24. 错题记录

25. 学习记录

26. 数据分析 Dashboard

27. 用户系统

28. 扩展更多 Reading 题型

29. Listening

30. Writing

31. Speaking
````

---

# 19. 当前最近目标

当前最近目标：

```text
真实 Reading JSON
        ↓
QuestionGroup / QuestionOption
        ↓
Vue 答题 UI
        ↓
用户选择答案
```

第一阶段只支持：

MATCHING_HEADINGS
MATCHING_FEATURES

完成目标：

A Brief History of Tea
↓
用户可以真正完成 Questions 1-13

然后继续进入：

Submit
↓
Spring Boot 判分
↓
返回正确 / 错误 / 正确答案
