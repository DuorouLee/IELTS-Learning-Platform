# IELTS Learning Platform

一个基于 **Vue 3 + Spring Boot + SQLite** 开发的 IELTS 学习平台。

本项目既是一个实际可使用的 IELTS 学习工具，也是一个用于展示 Java Web 全栈开发能力的个人项目。

目前项目已经完成 **IELTS Reading 核心练习闭环**，包括题库导入、阅读答题、后端判分、结果回顾、练习历史记录与统计等功能。

项目仍在持续开发中，后续将逐步扩展 Vocabulary、Writing、学习数据分析以及 AI 辅助学习能力。

---

## ✨ Project Goals

项目的主要目标：

- 构建一个完整的 IELTS 学习平台
- 使用 Spring Boot 实现统一业务后端
- 使用 Vue 3 构建前端学习界面
- 使用 SQLite 支持轻量化、本地化数据存储
- 使用 Flyway 管理数据库结构版本
- 建立 Reading / Vocabulary / Writing 等统一学习模型
- 保存用户练习历史并进行数据统计
- 支持 Web，并为后续 Windows 桌面端预留能力
- 后续通过独立 AI 服务实现写作评分和学习辅助

---

# 🛠 Tech Stack

## Backend

- Java 25
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Hibernate
- MyBatis
- Flyway
- SQLite
- Maven
- Jackson
- Bean Validation
- REST API

当前 Reading 模块主要采用：

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

Flyway 负责数据库 Schema 的版本管理。

---

## Frontend

- Vue 3
- TypeScript
- Vite
- Vue Router
- Browser Local Storage
- REST API

---

## Desktop

计划使用：

- Tauri

桌面端不会重新实现 IELTS 核心业务逻辑，而是复用现有 Vue 前端与 Spring Boot 后端。

---

## AI

计划使用独立 AI Runtime：

- Python

未来可用于：

- IELTS Writing 辅助评分
- Writing Feedback
- 学习建议生成
- 错题分析
- 个性化学习辅助

AI 模块目前仍处于后续规划阶段。

---

# 🏗 System Architecture

当前主要架构：

```text
┌──────────────────────────┐
│          Vue 3           │
│      TypeScript + Vite   │
└─────────────┬────────────┘
              │
              │ REST API
              ▼
┌──────────────────────────┐
│       Spring Boot        │
│                          │
│ Controller               │
│ Service                  │
│ Repository               │
│ Domain / Entity          │
└─────────────┬────────────┘
              │
              ▼
┌──────────────────────────┐
│          SQLite          │
│                          │
│ Spring Data JPA          │
│ Hibernate                │
│ Flyway Migration         │
└──────────────────────────┘
```

核心 IELTS 业务逻辑统一由 Spring Boot 管理。

Vue 主要负责：

- 页面展示
- 用户交互
- 答题状态管理
- 调用 REST API

SQLite 负责：

- IELTS 题库数据
- Reading 练习数据
- Practice History
- 后续学习记录

---

# 📚 Current Features

## IELTS Reading

Reading 是目前项目中完成度最高的模块。

已经形成完整的：

```text
加载题目
   ↓
用户答题
   ↓
保存临时答案
   ↓
提交答案
   ↓
后端判分
   ↓
显示结果
   ↓
逐题 Review
   ↓
保存 Practice History
```

---

## 1. Reading Data Model

目前 Reading 核心数据结构包括：

```text
ReadingTest
    │
    ├── ReadingPassage
    │
    ├── ReadingQuestionGroup
    │
    └── ReadingQuestion
```

支持将真实 IELTS Reading 数据转换为平台内部统一的数据模型。

这样可以避免业务代码直接依赖外部题库原始结构。

---

## 2. IELTS Reading JSON Import

项目已经支持真实 IELTS Reading JSON 数据导入。

外部题库数据经过导入逻辑转换后进入平台内部数据模型。

核心设计思想：

```text
External IELTS Data
        ↓
    Importer
        ↓
Internal Reading Model
        ↓
      SQLite
```

这样能够将：

```text
题库来源
```

与：

```text
IELTS 学习业务逻辑
```

进行解耦。

---

## 3. Reading Full Test API

Reading 页面通过：

```http
GET /api/reading/tests/{id}/full
```

获取完整 Reading Test。

返回内容包含：

- Test
- Passage
- QuestionGroup
- Question

为了避免前端提前获取答案：

```text
correctAnswer
```

不会在 `/full` API 中暴露。

---

## 4. Reading Practice Page

前端已经实现 IELTS Reading 双栏答题页面。

页面设计接近真实 IELTS 阅读练习场景：

```text
┌──────────────────────┬──────────────────────┐
│                      │                      │
│       Passage        │      Questions       │
│                      │                      │
│       阅读文章        │       答题区域        │
│                      │                      │
└──────────────────────┴──────────────────────┘
```

用户可以同时查看：

- Reading Passage
- Question Groups
- Questions

并直接完成答题。

---

## 5. Local Answer Persistence

Reading 答题过程中，用户答案会临时保存在：

```text
localStorage
```

因此页面刷新后，未提交答案仍然能够恢复。

该功能用于降低用户因为误刷新页面导致答题内容丢失的风险。

---

## 6. Reading Submission

用户完成 Reading 后，通过：

```http
POST /api/reading/tests/{id}/submit
```

提交答案。

判分逻辑由后端负责。

前端不会自行判断答案是否正确。

数据流：

```text
Vue
 │
 │ User Answers
 ▼
Spring Boot
 │
 │ Compare Answers
 ▼
Reading Evaluation
 │
 ▼
Result
```

---

## 7. Server-side Evaluation

Spring Boot 根据：

```text
User Answer
```

与：

```text
Correct Answer
```

进行比较并计算结果。

提交完成后返回：

- correct
- correctAnswer
- correctCount
- totalQuestions
- percentage

这样可以避免：

```text
correctAnswer
```

在正式提交之前暴露给前端。

---

## 8. Reading Result

提交完成后，页面显示本次 Reading 的整体成绩。

包括：

```text
Correct Count

Total Questions

Accuracy
```

例如：

```text
32 / 40

80%
```

---

## 9. Reading Review

提交后可以逐题查看答题情况。

Review 页面能够展示：

- 用户答案
- 正确答案
- 是否答对

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

正确答案只有在提交完成后才会由后端返回。

---

# 📊 Reading Practice History

Reading Practice History 已经完成数据库持久化。

每次 Reading 提交后，会保存一条 Practice Record。

当前记录字段：

```text
id

testId

testTitle

correctCount

totalQuestions

percentage

submittedAt
```

其中：

```text
submittedAt
```

当前使用 Unix Timestamp (`Long`) 保存。

这样可以避免 SQLite 与 Java `LocalDateTime` 在日期格式解析上的兼容问题。

---

## Practice History Database

Practice History 使用：

```text
reading_practice_record
```

表进行持久化。

数据库结构通过 Flyway Migration 管理。

当前相关 Migration 已进入：

```text
V11
```

---

## Practice History API

获取练习历史：

```http
GET /api/reading/practice-history
```

删除练习记录：

```http
DELETE /api/reading/practice-history/{id}
```

---

## Practice History Page

前端已经完成 Reading Practice History 页面。

可以查看历史练习记录，包括：

- Test Title
- Correct Count
- Total Questions
- Accuracy
- Submitted Time

History 页面还支持从历史记录重新进入对应 Reading Test。

---

## Practice Statistics

Practice History 页面已经实现基础统计功能。

目前包括：

```text
Practice Count
```

练习次数。

```text
Average Accuracy
```

平均正确率。

```text
Best Accuracy
```

历史最佳正确率。

---

## Delete Practice Record

History 页面支持删除指定 Reading 练习记录。

删除前会进行确认，防止误操作。

流程：

```text
Delete
   ↓
Confirmation
   ↓
DELETE API
   ↓
Database Delete
   ↓
Refresh History
```

---

# 🔐 Answer Protection

Reading 模块特别处理了答案提前泄露的问题。

### Before Submit

调用：

```http
GET /api/reading/tests/{id}/full
```

时：

```text
correctAnswer
```

不会发送给浏览器。

---

### After Submit

只有调用：

```http
POST /api/reading/tests/{id}/submit
```

完成提交之后，后端才会返回：

```text
correctAnswer

correct
```

用于 Result / Review 页面。

因此判分规则和正确答案由服务器端控制。

---

# 🗄 Database Migration

项目使用：

```text
Flyway
```

管理数据库 Schema。

数据库结构不会依赖开发者手动修改 SQLite 文件。

每一次结构变更都通过：

```text
V1
V2
V3
...
```

形式的 Migration SQL 进行版本控制。

例如：

```text
src/main/resources/db/migration/

V1__...
V2__...
...
V11__...
```

这种方式可以保证：

- 数据库结构可追踪
- 不同开发环境结构一致
- Schema 修改能够进入 Git 版本历史
- 数据库升级过程可重复执行

---

# 📁 Project Structure

```text
IELTS-Learning-Platform/
│
├── frontend/
│   └── Vue 3 + TypeScript frontend
│
├── backend/
│   └── Spring Boot backend
│
├── desktop/
│   └── Tauri desktop application
│
├── ai-runtime/
│   └── Future AI runtime
│
├── tools/
│   └── Dataset import and development tools
│
├── docs/
│   └── Project documentation
│
├── data/
│   └── Local development data
│
└── README.md
```

---

# 🌐 Core REST APIs

当前 Reading 模块的主要 API：

### Get Full Reading Test

```http
GET /api/reading/tests/{id}/full
```

获取完整 Reading Test，用于答题页面。

---

### Submit Reading Test

```http
POST /api/reading/tests/{id}/submit
```

提交 Reading 答案并由后端完成判分。

---

### Get Reading Practice History

```http
GET /api/reading/practice-history
```

获取 Reading 历史练习记录。

---

### Delete Reading Practice Record

```http
DELETE /api/reading/practice-history/{id}
```

删除指定练习记录。

---

# 🚀 Local Development

## Requirements

推荐开发环境：

```text
Java 25
Node.js
npm
Maven
Git
```

SQLite 不需要单独运行数据库服务器。

---

## 1. Clone Repository

```bash
git clone https://github.com/DuorouLee/IELTS-Learning-Platform.git

cd IELTS-Learning-Platform
```

---

## 2. Start Backend

进入：

```bash
cd backend
```

Windows：

```bash
mvnw.cmd spring-boot:run
```

或：

```bash
mvn spring-boot:run
```

Spring Boot 默认运行在：

```text
http://localhost:8080
```

---

## 3. Start Frontend

打开另一个终端：

```bash
cd frontend
```

安装依赖：

```bash
npm install
```

启动开发服务器：

```bash
npm run dev
```

然后根据 Vite 终端输出访问前端地址。

通常为：

```text
http://localhost:5173
```

---

# 📦 IELTS Dataset

本项目独立设计并实现：

- 系统架构
- Vue 前端
- Spring Boot 后端
- 数据库结构
- REST API
- Reading 业务逻辑
- Reading 判分逻辑
- Practice History
- Statistics
- 外部题库导入机制

IELTS 练习题目本身作为外部学习数据使用。

当前开发过程中参考的数据来源之一：

**IELTS Atlas**

https://github.com/sallowayma-git/IELTS-practice

外部来源中的：

- IELTS 文章
- Questions
- Answers
- Audio
- Images
- PDFs
- 其他学习资源

并非由本项目创作。

相关版权及使用规则归原始权利方所有。

大型外部题库文件不会直接提交到本仓库。

平台通过数据导入机制，将外部题库转换为内部统一的数据模型，从而避免核心业务逻辑直接依赖某一种数据来源。

---

# ✅ Current Development Status

## Completed

### Project Infrastructure

- [x] Git repository
- [x] Spring Boot backend
- [x] Vue 3 + TypeScript frontend
- [x] SQLite integration
- [x] Flyway migration
- [x] REST communication between frontend and backend

### Reading

- [x] Reading Test model
- [x] Reading Passage model
- [x] Reading Question model
- [x] Reading QuestionGroup model
- [x] Real IELTS Reading JSON import
- [x] Full Reading Test API
- [x] Reading dual-pane practice page
- [x] localStorage answer persistence
- [x] Reading submission API
- [x] Server-side answer evaluation
- [x] Reading Result
- [x] Per-question Review
- [x] Prevent correct answers from being exposed before submission

### Practice History

- [x] Reading Practice Record entity
- [x] Practice History database persistence
- [x] Flyway migration
- [x] Practice History Repository
- [x] Practice History Service
- [x] Practice History Controller
- [x] Practice History API
- [x] Reading Practice History page
- [x] Practice Count
- [x] Average Accuracy
- [x] Best Accuracy
- [x] Navigate from History back to Reading Test
- [x] Delete Practice History
- [x] Delete confirmation
- [x] Reading History entry on Home page

---

# 🧭 Roadmap

项目采用逐步迭代的方式开发。

当前阶段重点是先把一个业务模块完整做好，而不是同时开发大量未完成模块。

```text
Reading Core
     ✅
      ↓
Reading Submission
     ✅
      ↓
Reading Review
     ✅
      ↓
Practice History
     ✅
      ↓
Reading learning experience improvements
      ↓
Vocabulary
      ↓
Writing
      ↓
Learning Analytics
      ↓
Desktop Integration
      ↓
AI-assisted Learning
```

计划中的模块包括：

- Vocabulary Learning
- Vocabulary Review
- Writing Practice
- Learning Dashboard
- Long-term Learning Statistics
- Desktop Application
- AI Writing Feedback
- Personalized Study Assistance

Roadmap 中的功能表示未来开发方向，不代表当前已经完成。

---

# 💡 Engineering Highlights

这个项目不仅关注页面功能，也尝试实践完整的软件工程开发流程。

目前已经涉及：

- RESTful API Design
- Layered Backend Architecture
- Entity / Repository / Service / Controller separation
- Spring Data JPA persistence
- Hibernate ORM
- SQLite integration
- Flyway database version management
- DTO-based API response
- Server-side evaluation
- Frontend / Backend separation
- Vue 3 Composition API
- TypeScript
- Browser local persistence
- External dataset normalization
- Learning history persistence
- Basic learning statistics
- Git-based incremental development

---

# 🎯 Why This Project

IELTS Learning Platform 是一个长期迭代的个人全栈项目。

与单纯的 CRUD Demo 不同，本项目尝试围绕真实 IELTS 学习场景建立完整业务流程。

例如 Reading 模块已经覆盖：

```text
External Dataset
       ↓
Data Import
       ↓
Database
       ↓
REST API
       ↓
Vue Practice UI
       ↓
User Submission
       ↓
Server-side Evaluation
       ↓
Result / Review
       ↓
Practice Record
       ↓
History Statistics
```

项目将继续通过小规模、可验证的迭代逐步增加功能，并保持代码结构和业务模型的可维护性。

---

# 📌 Repository

GitHub:

https://github.com/DuorouLee/IELTS-Learning-Platform

---

## Status

🚧 **Under active development**

当前核心进度：

**IELTS Reading MVP + Practice History 已完成。**

项目正在持续迭代中。
