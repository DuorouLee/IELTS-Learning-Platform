# IELTS Learning Platform 开发路线

## 1. 项目目标

IELTS Learning Platform 是一个以 **Java + Spring Boot** 为核心后端的 IELTS 英语学习平台。

项目有两个主要目标：

1. 作为 Java 后端求职项目，练习完整的 Web 项目开发流程。
2. 作为个人 IELTS 学习工具，用于练习 Reading、记录错题和分析学习数据。

当前技术栈：

* Backend：Java / Spring Boot
* ORM：Spring Data JPA / Hibernate
* Database：SQLite
* Database Migration：Flyway
* Frontend：Vue 3 + TypeScript
* API：REST API
* Version Control：Git + GitHub

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

* Controller：负责接收 HTTP 请求和返回 HTTP 响应。
* Service：负责业务逻辑。
* Repository：负责数据库访问。
* Entity：负责 Java 对象和数据库表之间的映射。
* SQLite：保存 IELTS 题库和未来的学习数据。

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

* 创建 Spring Boot 项目。
* 配置 SQLite。
* 配置 Spring Data JPA。
* 配置 Flyway。
* Spring Boot 可以正常启动。
* 数据库可以正常创建和访问。

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

当前已经完成：

```text
Database
   ↑
Repository
   ↑
Service
   ↑
Controller
   ↑
REST API
```

下一步需要继续完成：

```text
Vue
 ↓
REST API
 ↓
Spring Boot
 ↓
SQLite
```

也就是完成第一次真正的前后端闭环。

---

# 6. 下一阶段：Vue 前端

## 目标

创建 Vue 3 前端，并成功调用：

```http
GET /api/reading/tests/1/full
```

最终网页能够显示：

```text
Reading Test Title

Passage 1

Passage Content

Question 1
Question 2
Question 3
```

当前阶段重点不是 UI，而是验证：

```text
Vue
 ↓
HTTP Request
 ↓
Spring Boot Controller
 ↓
Service
 ↓
Repository
 ↓
SQLite
```

整个链路是否正常工作。

---

## 前端初始结构

计划建立：

```text
frontend/src/

├── api/
│   └── reading.ts
│
├── views/
│   └── ReadingTestView.vue
│
├── router/
│   └── index.ts
│
├── App.vue
│
└── main.ts
```

其中：

### api

负责调用 Spring Boot REST API。

例如：

```text
GET /api/reading/tests/1/full
```

### views

负责显示完整页面。

例如：

```text
ReadingTestView.vue
```

### router

负责页面路由，例如未来：

```text
/reading/tests/1
```

---

# 7. Vue 完成之后：真实 IELTS 题库

Vue 与 Spring Boot 成功连接之后，再开始正式处理真实 Reading 题库。

这一阶段不能只是简单把题目文字插入数据库，而需要继续完善数据结构。

真实 IELTS Reading 包含很多题型，例如：

```text
TRUE_FALSE_NOT_GIVEN

YES_NO_NOT_GIVEN

MULTIPLE_CHOICE

MATCHING_HEADINGS

MATCHING_INFORMATION

MATCHING_FEATURES

MATCHING_SENTENCE_ENDINGS

SENTENCE_COMPLETION

SUMMARY_COMPLETION

NOTE_COMPLETION

TABLE_COMPLETION

FLOW_CHART_COMPLETION

DIAGRAM_LABEL_COMPLETION

SHORT_ANSWER
```

因此需要继续设计 Question 数据模型。

---

# 8. Question 模型升级

当前基础结构：

```text
ReadingQuestion
```

未来可能需要扩展：

```text
ReadingQuestion

id
questionNumber
questionType
questionText
correctAnswer
explanation
```

以及：

```text
QuestionOption
```

用于选择题等题型。

可能形成：

```text
ReadingQuestion
    ↓
QuestionOption
```

例如：

```text
Question 1

A. ...
B. ...
C. ...
D. ...
```

---

# 9. 真实题库导入

真实题库阶段需要考虑一个重要问题：

不要手工逐条通过 SQL 插入大量题目。

计划建立统一的数据导入机制，例如：

```text
JSON
 ↓
Java Import Service
 ↓
ReadingTest
 ↓
ReadingPassage
 ↓
ReadingQuestion
 ↓
SQLite
```

这样以后可以通过 JSON 文件维护题库。

例如：

```text
data/
└── reading/
    ├── cambridge-18-test-1.json
    ├── cambridge-18-test-2.json
    └── ...
```

这种方式也更适合作为 Java 项目展示。

---

# 10. Reading 答题系统

真实题库完成以后，再开发答题功能。

核心流程：

```text
用户打开 Reading Test
        ↓
阅读 Passage
        ↓
填写答案
        ↓
Submit
        ↓
Spring Boot
        ↓
判断答案
        ↓
返回 Result
```

需要新增类似：

```text
ReadingSubmission
ReadingAnswer
```

的数据结构。

---

# 11. 判分系统

后端负责进行答案判断。

例如：

```text
User Answer
    ↓
ReadingService
    ↓
Correct Answer
    ↓
Compare
    ↓
Score
```

最终返回：

```text
Correct
Incorrect
Score
Explanation
```

---

# 12. 错题系统

答题功能完成以后，增加：

```text
WrongAnswer
```

记录：

```text
哪一套 Test
哪个 Passage
哪一道 Question
用户答案
正确答案
题型
时间
```

未来可以建立：

```text
Wrong Answer Book
```

用于 IELTS 错题复习。

---

# 13. 学习记录

后续增加：

```text
StudySession
```

记录：

```text
练习日期
Test
正确率
完成时间
题型错误情况
```

逐渐形成真正的 IELTS Learning Platform。

---

# 14. 数据分析

有了学习数据之后，可以实现：

```text
Reading Dashboard
```

例如：

```text
总练习次数

平均正确率

最近 7 天正确率

最容易错的题型

TRUE/FALSE/NOT GIVEN 正确率

Matching Headings 正确率
```

这一部分可以用于展示 Java 后端的数据统计能力。

---

# 15. 用户系统

Reading 核心功能完成后，再加入用户系统。

例如：

```text
User
```

后续可以加入：

```text
Spring Security
JWT
Login
Register
```

这样不同用户可以保存自己的学习数据。

---

# 16. 长期模块规划

Reading 模块稳定后，再逐步扩展：

```text
IELTS Learning Platform

├── Reading
├── Listening
├── Writing
└── Speaking
```

其中 Reading 作为第一个完整模块。

核心原则：

```text
先完整做好一个模块
再复制架构到其他 IELTS 模块
```

---

# 17. 当前执行顺序

目前严格按照以下顺序开发：

```text
1. Spring Boot 基础                ✅

2. SQLite + Flyway                ✅

3. ReadingTest                    ✅

4. ReadingPassage                 ✅

5. ReadingQuestion                ✅

6. Reading 完整 REST API          ✅

7. Vue 3 初始化                   ← 当前

8. Vue 调用 Reading API

9. Reading 页面

10. 真实 IELTS Reading 题库

11. Question 数据模型升级

12. JSON 题库导入系统

13. Reading 答题

14. Reading 判分

15. 错题记录

16. 学习记录

17. 数据分析

18. 用户系统

19. Listening

20. Writing

21. Speaking
```

---

# 18. 开发原则

项目开发过程中遵循以下原则：

### 每次只完成一个小功能

例如：

```text
Entity
↓
Repository
↓
Service
↓
Controller
↓
Test
```

确认完成以后再进入下一阶段。

### 每个阶段都使用 Git 留痕

完成一个独立功能后：

```bash
git status
git add .
git commit -m "..."
git push
```

保证 GitHub 能完整展示项目的开发过程。

### 优先理解代码

这个项目不仅要求功能可以运行，还要求理解：

```text
为什么需要 Controller

为什么需要 Service

为什么需要 Repository

数据库关系如何建立

HTTP 请求如何进入 Java

Java 如何读取数据库

前端如何调用后端
```

最终目标是不只是“项目能跑”，而是能够在 Java 面试中解释整个项目。

---

# 19. 当前最近目标

当前最近目标：

```text
Vue 页面
   ↓
Reading REST API
   ↓
Spring Boot
   ↓
SQLite
```

成功显示第一套 Reading Test。

完成这个目标后，再正式进入：

```text
真实 IELTS Reading 题库设计与导入
```
