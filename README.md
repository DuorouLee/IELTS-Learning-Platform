# IELTS Learning Platform

A full-stack IELTS learning platform built with Java, Spring Boot, Vue 3 and SQLite.

这是一个用于 IELTS 学习与 Java Web 开发实践的个人项目。

项目当前重点围绕 Reading 和 Vocabulary 两个模块，目标是构建一个具备真实学习流程、后端业务逻辑、数据持久化和学习记录能力的 IELTS 学习平台。

---

## 1. Product Overview

IELTS Learning Platform 主要解决两类需求：

1. 提供可持续使用的 IELTS 学习工具
2. 作为 Java / Spring Boot 求职项目展示完整 Web 开发能力

当前已实现：

- IELTS Reading Practice
- Reading 题库导入
- Reading 后端判分
- Reading 答案 Review
- Reading Practice History
- Vocabulary Management
- Vocabulary Practice
- Vocabulary Learning Status

当前主要开发方向：

- Reading 机考体验优化
- Reading 解析与原文定位
- Reading 学习数据分析
- Vocabulary 学习体验优化

---

## 2. Tech Stack

### Backend

- Java
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- Hibernate
- SQLite
- Flyway
- Maven
- Jackson
- Bean Validation
- JUnit
- MockMvc

### Frontend

- Vue 3
- TypeScript
- Vite
- Vue Router
- REST API

### Database

- SQLite
- Flyway Migration

### Version Control

- Git
- GitHub

---

## 3. System Architecture

项目采用前后端分离架构：

```text
Vue 3
  ↓
REST API
  ↓
Spring Boot
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

核心业务规则主要由 Spring Boot Service 层负责。

Vue 前端主要负责：

- 页面展示
- 用户交互
- 答题状态
- API 调用
- 学习结果展示

数据库 Schema 统一通过 Flyway 管理。

---

## 4. IELTS Reading

Reading 是目前项目的核心模块。

完整学习流程：

```text
Reading Test
    ↓
Load Passage & Questions
    ↓
Answer Questions
    ↓
Submit
    ↓
Server-side Evaluation
    ↓
Result
    ↓
Review
    ↓
Practice History
```

### 4.1 Reading Data Model

核心数据结构：

```text
ReadingTest
    │
    └── ReadingPassage
            │
            └── QuestionGroup
                    │
                    ├── QuestionOption
                    │
                    └── ReadingQuestion
```

练习记录：

```text
ReadingPracticeRecord
        │
        └── ReadingPracticeAnswer
```

其中：

- `ReadingTest`：一套完整 Reading Test
- `ReadingPassage`：Reading Passage 1 / 2 / 3
- `QuestionGroup`：Questions 1–7、Questions 14–17 等题组
- `QuestionOption`：Matching 等题型共享选项
- `ReadingQuestion`：单道题目、答案、解析及独立选项

### 4.2 Reading Question Types

Reading 数据模型支持不同题型采用不同结构。

#### Multiple Choice

每一道题可以拥有独立选项：

```text
Question 27
├── A
├── B
├── C
└── D
```

#### Matching

整个 Question Group 可以共享一组选项：

```text
Questions 14–17

A
B
C
D
E
F
G
```

这种设计用于适配不同 IELTS Reading 题型，而不是把所有题目强制存成同一种结构。

### 4.3 Reading Data Import

项目支持将外部 Reading JSON 转换为平台内部统一格式。

```text
External Reading Data
        ↓
Converter
        ↓
Reading Import JSON
        ↓
ReadingImportService
        ↓
Reading Domain Model
        ↓
SQLite
```

外部数据格式与平台内部业务模型相互解耦，题库数据统一转换后，再由 Spring Boot 负责保存。

### 4.4 Reading Practice

Reading Practice 页面采用 Passage 与 Questions 双栏结构：

```text
┌──────────────────────┬──────────────────────┐
│                      │                      │
│       Passage        │      Questions       │
│                      │                      │
│      阅读文章          │       答题区域        │
│                      │                      │
└──────────────────────┴──────────────────────┘
```

当前练习流程支持：

- Passage 阅读
- Question Group 展示
- 用户答题
- 未完成答案暂存
- 提交答案
- 后端判分
- 查看正确答案
- 查看错题
- Practice History

### 4.5 Answer Evaluation

Reading 判分由 Spring Boot 后端完成。

```text
User Answer
    ↓
Reading Service
    ↓
Correct Answer
    ↓
Evaluation
    ↓
Result
```

提交后返回：

- Correct / Incorrect
- Correct Answer
- Correct Count
- Total Questions
- Accuracy

### 4.6 Reading Review

完成练习后可以查看：

- 用户答案
- 正确答案
- 是否答对
- Question Explanation

Reading 数据模型也为后续功能保留扩展空间：

- Passage Translation
- Answer Source Highlight
- Original Text Highlight
- Question-specific Review

### 4.7 Reading Practice History

每次提交后都会保存练习记录，包括：

- Test
- Score
- Correct Count
- Total Questions
- Accuracy
- Submitted Time
- 每道题的用户答案

用户可以重新进入历史记录查看某一次完整练习结果。

---

## 5. Vocabulary

Vocabulary 模块目前已经完成基础学习闭环：

```text
Add Word
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

当前支持：

- 添加单词
- 删除单词
- 单词释义
- Example Sentence
- Learning Status
- Vocabulary Practice
- Learning / Mastered 状态切换
- 基础数量统计

核心实体：

```text
VocabularyWord
├── word
├── meaning
├── exampleSentence
├── learningStatus
└── createdAt
```

---

## 6. Backend Design

Spring Boot 后端按照典型分层架构组织：

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

### Controller

负责：

- HTTP Request
- Request Parameter
- Response

### Service

负责：

- 业务规则
- 数据转换
- 判分
- 状态校验
- 导入逻辑

### Repository

负责：

- 数据库访问
- Spring Data JPA

### Entity

负责：

- Domain Model
- Database Mapping

---

## 7. Database Migration

项目使用 Flyway 管理 SQLite Schema。

Migration 文件位于：

```text
backend/src/main/resources/db/migration/
```

数据库结构通过版本化 migration 持续演进：

```text
V1__...
V2__...
V3__...
...
```

这样可以避免手动修改数据库造成不同环境结构不一致。

---

## 8. Automated Testing

后端使用：

- JUnit
- Spring Boot Test
- MockMvc

用于测试：

- REST API
- Service Business Logic
- Validation
- Error Handling

运行：

```bash
cd backend
mvn test
```

---

## 9. Project Structure

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
│       │   │       └── vocabulary
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
│       ├── components
│       ├── router
│       └── views
│
├── tools
│   └── import-reading
│
├── data
├── docs
├── desktop
└── ai-runtime
```

---

## 10. Running the Project

### Backend

```bash
cd backend
mvn spring-boot:run
```

Default:

```text
http://localhost:8080
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Default:

```text
http://localhost:5173
```

### Backend Tests

```bash
cd backend
mvn test
```

### Frontend Build

```bash
cd frontend
npm run build
```

---

## 11. Engineering Highlights

### Server-side Business Logic

Reading 判分、Vocabulary 状态校验等核心规则由 Spring Boot 控制。

### Reading Domain Modeling

针对 Passage、Question Group、Question、Shared Options 和 Question-specific Options 建立独立数据模型。

### External Data Conversion

外部 Reading 数据先通过 Converter 转换，再进入统一的 Reading Import Model。

### Database Version Control

所有 Schema 修改通过 Flyway 管理，并与 Git 一起维护。

### REST API

Vue 与 Spring Boot 通过 REST API 通信，前端不直接操作数据库。

### Practice History

Reading 不只是完成一次答题，还保存完整 Practice Record 和逐题答案，用于后续 Review 和学习分析。

---

## 12. Roadmap

当前 Reading 的下一阶段重点：

```text
Reading Practice UI
        ↓
Resizable Split View
        ↓
Question-specific Rendering
        ↓
Translation
        ↓
Answer Explanation
        ↓
Original Text Highlight
        ↓
Learning Analytics
```

计划进一步完善：

- 更接近 IELTS Computer-delivered Test 的 Reading UI
- Passage / Questions 可调节双栏布局
- 不同题型独立交互组件
- Passage Translation
- Answer Explanation
- Answer Source Highlight
- 错题分析
- Reading Statistics
- Learning Analytics

---

## Project Status

项目处于持续开发阶段。

当前重点：

**完善 Reading 的真实题库、数据模型、答题体验和 Review 学习闭环。**
