# IELTS Learning Platform

A full-stack IELTS learning platform built with **Java, Spring Boot, Vue 3 and SQLite**.

本项目既是一个实际可使用的 IELTS 学习工具，也是一个用于展示 **Java Web / Spring Boot 全栈开发能力** 的个人项目。

目前已经完成两个核心学习模块：

- ✅ IELTS Reading
- ✅ Vocabulary Learning

Reading 已形成从题库导入、答题、后端判分、Review 到 Practice History 的完整学习闭环。

Vocabulary 已形成从单词管理、学习状态、Vocabulary Practice 到基础学习统计的第一版学习闭环。

项目仍在持续开发中，下一阶段将扩展：

- Writing
- Learning Analytics
- Desktop Integration
- AI-assisted Learning

---

# ✨ Project Goals

本项目主要有两个目标。

## 1. Java 后端求职项目

通过实际业务功能练习和展示：

- Java
- Spring Boot
- REST API
- Spring Data JPA
- Hibernate
- SQLite
- Flyway
- 分层架构
- Exception Handling
- Automated Testing
- 前后端分离
- Git / GitHub 开发流程

项目后端保持统一架构：

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Spring Data JPA / Hibernate
    ↓
SQLite
```

业务规则主要放在 Service 层，Controller 负责 HTTP 接口，Repository 负责数据库访问。

---

## 2. IELTS Learning Tool

平台同时用于实际 IELTS 学习，目前主要覆盖：

```text
Reading
Vocabulary
Writing (planned)
```

未来会逐步加入：

```text
Learning History
Statistics
Learning Analytics
AI Feedback
Writing Assistance
```

---

# 🛠 Tech Stack

## Backend

- Java 25
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Hibernate
- Flyway
- SQLite
- Maven
- Jackson
- Bean Validation
- REST API
- JUnit
- MockMvc

主要后端调用链：

```text
HTTP Request
     ↓
Controller
     ↓
Service
     ↓
Repository
     ↓
JPA / Hibernate
     ↓
SQLite
```

---

## Frontend

- Vue 3
- TypeScript
- Vite
- Vue Router
- Fetch API
- Browser Local Storage

Vue 主要负责：

- 页面展示
- 用户交互
- 前端状态管理
- Reading 草稿保存
- REST API 调用
- Practice 页面交互

---

## Database

- SQLite
- Flyway Migration

所有数据库 Schema 变更通过 Flyway 管理：

```text
V1__...
V2__...
V3__...
...
```

数据库结构不依赖开发者手动修改 SQLite 文件。

---

## Desktop

计划使用：

- Tauri

桌面端不会重新实现 IELTS 核心业务逻辑，而是尽量复用：

```text
Vue Frontend
+
Spring Boot Backend
```

---

## AI Runtime

计划使用独立 AI Runtime：

- Python

未来可能用于：

- IELTS Writing Feedback
- Writing 辅助评分
- 错题分析
- 学习建议生成
- 个性化学习辅助

AI 模块目前仍属于后续开发阶段。

---

# 🏗 System Architecture

当前核心架构：

```text
┌─────────────────────────────┐
│          Vue 3              │
│     TypeScript + Vite       │
│                             │
│ Reading / Vocabulary UI     │
└──────────────┬──────────────┘
               │
               │ REST API
               ▼
┌─────────────────────────────┐
│        Spring Boot          │
│                             │
│ Controller                  │
│      ↓                      │
│ Service                     │
│      ↓                      │
│ Repository                  │
│      ↓                      │
│ Entity / Domain             │
└──────────────┬──────────────┘
               │
               │ JPA / Hibernate
               ▼
┌─────────────────────────────┐
│          SQLite             │
│                             │
│ Flyway Migration            │
└─────────────────────────────┘
```

核心 IELTS 业务逻辑统一由 Spring Boot 管理。

---

# 📚 Current Features

# 1. IELTS Reading

Reading 是目前项目中功能最完整的模块。

完整学习流程：

```text
Reading Test
     ↓
Load Passage / Questions
     ↓
Answer Questions
     ↓
Local Draft Persistence
     ↓
Submit Answers
     ↓
Server-side Evaluation
     ↓
Result
     ↓
Question Review
     ↓
Practice History
     ↓
History Detail / Statistics
```

---

## Reading Data Model

Reading 核心数据模型包括：

```text
ReadingTest
    │
    ├── ReadingPassage
    │
    ├── ReadingQuestionGroup
    │
    └── ReadingQuestion
```

同时包含练习记录：

```text
ReadingPracticeRecord
        │
        └── ReadingPracticeAnswer
```

这样既可以保存：

- 一次练习整体结果
- 每一道题的历史答案

---

## IELTS Reading JSON Import

项目已经支持 IELTS Reading JSON 数据导入。

数据流：

```text
External IELTS JSON
        ↓
Reading Importer
        ↓
Internal Reading Model
        ↓
Spring Data JPA
        ↓
SQLite
```

这样可以把：

```text
外部 IELTS 数据格式
```

与：

```text
平台内部业务模型
```

解耦。

---

## Reading Full Test API

获取完整 Reading Test：

```http
GET /api/reading/tests/{id}/full
```

返回内容包含：

- Reading Test
- Passages
- Question Groups
- Questions

为了防止答案提前泄露：

```text
correctAnswer
```

不会在 `/full` API 中返回。

---

## Reading Practice Page

Vue 前端实现了 IELTS Reading 双栏答题界面：

```text
┌──────────────────────────┬──────────────────────────┐
│                          │                          │
│        Passage           │        Questions         │
│                          │                          │
│        阅读文章           │         答题区域          │
│                          │                          │
└──────────────────────────┴──────────────────────────┘
```

用户可以同时阅读 Passage 并完成对应 Questions。

---

## Local Draft Persistence

未提交的 Reading 答案会暂存在：

```text
localStorage
```

因此：

```text
答题
 ↓
刷新页面
 ↓
恢复未完成答案
```

从 Home 页面主动开始一次新 Practice 时，则会清除对应旧草稿：

```text
Start New Practice
       ↓
Clear Old Draft
       ↓
Reading Test
       ↓
0 Answered
```

从而区分：

- 刷新页面 → 恢复草稿
- 新开始练习 → 清空草稿

---

## Reading Submission

用户完成 Reading 后调用：

```http
POST /api/reading/tests/{id}/submit
```

数据流：

```text
Vue
 │
 │ User Answers
 ▼
Spring Boot
 │
 │ Server-side Evaluation
 ▼
Result
```

判分逻辑由后端完成。

---

## Server-side Evaluation

Spring Boot 根据：

```text
User Answer
     ↓
Correct Answer
     ↓
Evaluation
```

生成：

- correct
- correctAnswer
- correctCount
- totalQuestions
- percentage

正确答案只有提交以后才会返回给浏览器。

---

## Reading Result & Review

提交以后可以查看：

```text
Score
Accuracy
Question Result
User Answer
Correct Answer
```

例如：

```text
Question 1

Your Answer:
FALSE

Correct Answer:
TRUE

Result:
Incorrect
```

---

# 📊 Reading Practice History

每次 Reading 提交后都会保存 Practice Record。

包括：

```text
id
testId
testTitle
correctCount
totalQuestions
percentage
submittedAt
```

同时每道题的历史答案保存到：

```text
ReadingPracticeAnswer
```

因此 History Detail 可以恢复某一次练习的逐题情况。

---

## Reading Practice History API

获取历史记录：

```http
GET /api/reading/practice-history
```

获取单次历史详情：

```http
GET /api/reading/practice-history/{id}
```

删除练习记录：

```http
DELETE /api/reading/practice-history/{id}
```

---

## Reading Practice Statistics

History 页面目前支持基础统计：

```text
Practice Count
Average Accuracy
Best Accuracy
```

用于快速查看 Reading 学习情况。

---

# 🔐 Answer Protection

Reading 模块专门处理了正确答案提前泄露的问题。

## Before Submit

调用：

```http
GET /api/reading/tests/{id}/full
```

时不会返回：

```text
correctAnswer
```

## After Submit

只有调用：

```http
POST /api/reading/tests/{id}/submit
```

完成提交以后，后端才返回：

```text
correctAnswer
correct
```

因此：

```text
Correct Answer
+
Evaluation Rule
```

由服务器端控制。

---

# 🧠 Vocabulary Learning

Vocabulary 已经完成第一版学习闭环。

整体流程：

```text
Add Vocabulary Word
        ↓
SQLite Persistence
        ↓
LEARNING
        ↓
Vocabulary Practice
        ↓
Recall Meaning
        ↓
Show Answer
        ↓
Still Learning / I Know
        ↓
LEARNING / MASTERED
```

---

## Vocabulary Data Model

核心实体：

```text
VocabularyWord
├── id
├── word
├── meaning
├── exampleSentence
├── learningStatus
└── createdAt
```

当前支持两个学习状态：

```text
LEARNING
MASTERED
```

新创建的单词默认：

```text
LEARNING
```

---

## Vocabulary REST API

当前实现：

```http
GET    /api/vocabulary/words
GET    /api/vocabulary/words/{id}
POST   /api/vocabulary/words
DELETE /api/vocabulary/words/{id}
PUT    /api/vocabulary/words/{id}/status
```

支持：

- 获取全部单词
- 根据 ID 查询单词
- 添加单词
- 删除单词
- 更新学习状态
- 重复单词校验
- 不存在单词的 404 处理
- 非法学习状态的 400 处理

---

## Vocabulary Business Rules

### Duplicate Word Protection

创建单词前：

```text
POST Vocabulary Word
        ↓
Service
        ↓
existsByWord()
        ↓
Exists?
 ┌──────┴──────┐
Yes            No
 ↓              ↓
400            save()
```

重复单词不会直接依赖数据库 UNIQUE 异常，而是由 Service 层主动进行业务校验。

---

## Learning Status Validation

当前只允许：

```text
LEARNING
MASTERED
```

即使绕过 Vue 前端直接调用 REST API：

```json
{
  "learningStatus": "INVALID"
}
```

后端 Service 仍会进行业务校验并拒绝非法状态。

这保证业务规则不是只存在于前端。

---

## Vocabulary Frontend

Vocabulary 页面目前支持：

- Vocabulary Word 列表
- Add Word
- Delete Word
- Meaning
- Example Sentence
- Learning Status
- Mark as Mastered
- Mark as Learning
- Total / Learning / Mastered statistics
- Vocabulary Practice 入口

---

## Vocabulary Practice

Practice 页面主要采用主动回忆：

```text
English Word
      ↓
Recall Meaning
      ↓
Show Answer
      ↓
Meaning + Example
      ↓
Choose Learning Result
```

用户可以选择：

```text
Still Learning
      ↓
LEARNING
```

或者：

```text
I Know
  ↓
MASTERED
```

Practice 优先使用：

```text
learningStatus = LEARNING
```

的单词。

已经 MASTERED 的单词不会继续出现在当前基础 Practice 列表中。

---

## Vocabulary Statistics

Vocabulary 首页可以查看：

```text
Total
Learning
Mastered
```

Practice 页面同时记录本轮基础完成情况。

---

# ⚠️ Exception Handling

项目提供统一：

```text
GlobalExceptionHandler
```

将业务异常转换成清晰的 HTTP Response。

当前包括：

```text
ReadingTestNotFoundException
        ↓
404 Not Found

VocabularyWordNotFoundException
        ↓
404 Not Found

DuplicateVocabularyWordException
        ↓
400 Bad Request

InvalidVocabularyLearningStatusException
        ↓
400 Bad Request
```

这样 Controller 不需要重复处理异常 Response。

---

# 🧪 Automated Testing

后端使用：

- JUnit
- Spring Boot Test
- MockMvc

测试覆盖 Reading 和 Vocabulary 的核心业务流程。

Vocabulary Controller Test 当前覆盖：

- Create Vocabulary Word
- Duplicate Word
- Get Word by ID
- Word Not Found
- Delete Word
- Update Learning Status
- Invalid Learning Status

测试方式：

```bash
cd backend
mvn test
```

目标：

```text
BUILD SUCCESS
```

---

# 🗄 Database Migration

项目使用 Flyway 管理 SQLite Schema。

Migration 文件位于：

```text
backend/src/main/resources/db/migration/
```

所有 Schema 修改都通过：

```text
V1__...
V2__...
V3__...
...
```

形式持续演进。

例如：

```text
Create Reading Tables
        ↓
Add Reading History
        ↓
Add Practice Answers
        ↓
Create Vocabulary Word
        ↓
Add Vocabulary Learning Status
```

避免直接手工修改数据库结构导致不同开发环境 Schema 不一致。

---

# 🌐 CORS Configuration

开发环境：

```text
Vue
http://localhost:5173

Spring Boot
http://localhost:8080
```

由于前后端端口不同，Spring Boot 通过 Web MVC CORS Configuration 允许 Vue 调用：

```text
/api/**
```

包括：

```text
GET
POST
PUT
DELETE
OPTIONS
```

---

# 📂 Project Structure

```text
IELTS-Learning-Platform
│
├── backend
│   └── src
│       ├── main
│       │   ├── java
│       │   │   └── com.duorou.ieltsbackend
│       │   │       ├── common
│       │   │       ├── config
│       │   │       ├── reading
│       │   │       │   ├── controller
│       │   │       │   ├── dto
│       │   │       │   ├── entity
│       │   │       │   ├── exception
│       │   │       │   ├── importer
│       │   │       │   ├── repository
│       │   │       │   └── service
│       │   │       │
│       │   │       └── vocabulary
│       │   │           ├── controller
│       │   │           ├── entity
│       │   │           ├── exception
│       │   │           ├── repository
│       │   │           └── service
│       │   │
│       │   └── resources
│       │       └── db
│       │           └── migration
│       │
│       └── test
│
├── frontend
│   └── src
│       ├── api
│       ├── router
│       └── views
│
├── data
├── docs
├── desktop
├── ai-runtime
└── tools
```

---

# ▶️ Running the Project

## 1. Start Backend

进入：

```bash
cd backend
```

运行：

```bash
mvn spring-boot:run
```

默认地址：

```text
http://localhost:8080
```

---

## 2. Start Frontend

进入：

```bash
cd frontend
```

安装依赖：

```bash
npm install
```

运行：

```bash
npm run dev
```

默认地址：

```text
http://localhost:5173
```

---

## 3. Run Backend Tests

```bash
cd backend
mvn test
```

---

## 4. Build Frontend

```bash
cd frontend
npm run build
```

---

# 💡 Engineering Highlights

这个项目重点展示的不只是页面功能，也包括后端工程实践。

## Layered Architecture

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

不同职责保持分离。

---

## Server-side Business Logic

Reading 判分规则和 Vocabulary 状态规则由后端控制，而不是依赖 Vue。

例如：

```text
Reading Answer Evaluation
Vocabulary Duplicate Validation
Vocabulary Learning Status Validation
```

都由 Spring Boot Service 处理。

---

## Database Version Control

使用 Flyway：

```text
Migration as Code
```

数据库 Schema 与 Git 代码一起管理。

---

## REST API Design

项目使用 RESTful API 连接 Vue 与 Spring Boot：

```text
GET
POST
PUT
DELETE
```

并通过：

```text
200 OK
204 No Content
400 Bad Request
404 Not Found
```

表达不同业务结果。

---

## Global Exception Handling

统一处理业务异常：

```text
Business Exception
       ↓
GlobalExceptionHandler
       ↓
HTTP Response
```

避免异常处理逻辑散落在不同 Controller 中。

---

## Automated Testing

通过 Spring Boot Test + MockMvc 自动验证 REST API 和业务行为。

开发过程采用：

```text
Failing Test
    ↓
Implement Feature
    ↓
BUILD SUCCESS
```

的方式逐步补充功能。

---

## Frontend / Backend Separation

Vue 不直接操作数据库。

完整数据流：

```text
Vue
 ↓
REST API
 ↓
Spring Boot
 ↓
JPA / Hibernate
 ↓
SQLite
```

---

# 🎯 Resume / Interview Highlights

如果用于 Java 后端求职展示，本项目目前可以重点介绍：

- 使用 Java + Spring Boot 构建 RESTful Web Backend
- 采用 Controller / Service / Repository 分层架构
- 使用 Spring Data JPA + Hibernate 实现数据库持久化
- 使用 SQLite 作为轻量本地数据库
- 使用 Flyway 管理数据库 Schema Migration
- 实现 Reading 多实体业务模型与 JSON Import
- 实现 Server-side Reading Evaluation，防止答案提前泄露
- 实现 Reading Practice History 与逐题历史答案持久化
- 使用 GlobalExceptionHandler 统一处理业务异常
- 实现 Vocabulary Learning State 与 Service 层业务校验
- 使用 JUnit + MockMvc 编写 REST API 自动化测试
- 使用 Vue 3 + TypeScript 实现前后端分离
- 使用 Git / GitHub 持续记录功能开发过程

---

# 🗺 Development Roadmap

当前开发路线：

```text
Reading Core
     ✅
      ↓
Reading Submission / Review
     ✅
      ↓
Reading Practice History
     ✅
      ↓
Vocabulary Management
     ✅
      ↓
Vocabulary Practice
     ✅
      ↓
Writing
     🚧
      ↓
Learning Analytics
      ⏳
      ↓
Desktop Integration
      ⏳
      ↓
AI-assisted Learning
      ⏳
```

---

# ✅ Current Status

## Completed

### Reading

- ✅ Reading Test / Passage / Question / QuestionGroup
- ✅ IELTS Reading JSON Import
- ✅ Reading Full Test API
- ✅ Dual-column Reading Practice
- ✅ localStorage Draft Persistence
- ✅ Server-side Evaluation
- ✅ Result / Review
- ✅ Answer Protection
- ✅ Practice History
- ✅ Practice Statistics
- ✅ History Delete
- ✅ History Detail
- ✅ Per-question History Persistence
- ✅ Automated Tests
- ✅ Global Exception Handling

### Vocabulary

- ✅ VocabularyWord Persistence
- ✅ Vocabulary REST API
- ✅ Add Word
- ✅ Delete Word
- ✅ Duplicate Validation
- ✅ Learning / Mastered Status
- ✅ Invalid Status Validation
- ✅ Vocabulary List Page
- ✅ Vocabulary Statistics
- ✅ Vocabulary Practice
- ✅ Still Learning / I Know Workflow
- ✅ Automated API Tests

---

## Next

下一阶段：

```text
IELTS Writing
```

计划逐步实现：

- Writing Task
- User Essay
- Writing Submission
- Writing History
- Feedback Model
- 后续 AI-assisted Feedback

---

# 📌 Project Status

**Reading MVP：Completed ✅**

**Vocabulary MVP：Completed ✅**

**Writing：Next 🚧**

项目仍在持续开发中。
