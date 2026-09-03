# IELTS Learning Platform

一个基于 **Vue 3、Spring Boot 和 SQLite** 从零设计与开发的跨平台 IELTS 学习平台。

项目目标是构建一个支持 **阅读练习、词汇复习、写作练习、学习记录、数据统计和离线学习** 的完整 IELTS 学习系统，并在后续逐步接入 AI 辅助学习能力。

## 技术栈

### Backend

- Java 25
- Spring Boot 4
- Maven
- MyBatis
- Flyway
- SQLite

### Frontend

- Vue 3
- TypeScript
- Vite

### Desktop

- Tauri

### AI

- Python

## 系统架构

当前核心架构：

```text
Vue 3
  |
REST API
  |
Spring Boot
  |
SQLite
```

后续 Windows 桌面端：

```text
Tauri
  |
Vue 3
  |
REST API
  |
Spring Boot
  |
SQLite
```

核心业务逻辑统一由 Spring Boot 负责，Vue 只负责界面和用户交互。

Tauri 后续仅负责 Windows 桌面环境相关能力，不承担 IELTS 核心业务逻辑。

## 核心设计目标

- 使用 Spring Boot 作为统一业务后端
- 支持 Web 和 Windows 桌面端
- 使用 SQLite 支持本地离线学习
- 将业务逻辑与具体 IELTS 题库来源解耦
- 统一记录 Reading、Vocabulary、Writing 等学习行为
- 基于学习数据提供历史记录和统计分析
- 后续通过独立 AI 服务接入写作评分和学习辅助能力

## 项目结构

```text
IELTS-Learning-Platform/
├── frontend/       # Vue 3 前端
├── backend/        # Spring Boot 后端
├── desktop/        # Tauri 桌面端
├── ai-runtime/     # AI 服务
├── tools/          # 数据导入等辅助工具
├── docs/           # 项目文档
└── data/           # 本地数据目录，不提交大型外部题库
```

## 题库与数据说明

本项目独立设计和开发以下内容：

- 系统架构
- 前端页面与交互
- Spring Boot 后端
- 数据库结构
- REST API
- IELTS 学习业务逻辑
- 学习历史和统计
- 外部题库导入机制

IELTS 练习题库作为外部数据源使用。

当前计划用于本地开发和测试的数据来源之一：

- IELTS Atlas
  https://github.com/sallowayma-git/IELTS-practice

外部来源中的 IELTS 文章、题目、答案、音频、PDF、图片及其他学习资源并非由本项目创作，其版权和使用规则归原始权利方所有。

平台不会让核心业务代码直接依赖 IELTS Atlas 的原始数据结构，而是通过题库导入器将外部资源转换为平台内部统一的数据模型。

大型外部题库资源不会直接提交到本仓库。

## 当前状态

项目目前处于基础架构开发阶段。

已完成：

- [x] 创建独立 Git 仓库
- [x] 建立项目基础目录
- [x] 完成项目 README 和技术路线设计

当前开发：

- [ ] 初始化 Spring Boot 后端
- [ ] 建立第一个 REST API
- [ ] 接入 SQLite 和 Flyway
- [ ] 初始化 Vue 3 前端
- [ ] 建立 Vue 与 Spring Boot 通信

## 开发路线

```text
基础架构
   ↓
Reading MVP
   ↓
学习历史与统计
   ↓
Vocabulary
   ↓
Writing
   ↓
AI 辅助
   ↓
Windows Desktop
```

第一阶段优先完成：

```text
Vue
 ↓
Spring Boot
 ↓
SQLite
```

在基础链路稳定之前，不提前引入 Cloud Sync、登录系统或其他复杂基础设施。

## 项目原则

- 每个阶段保持项目可运行
- 优先完成完整业务闭环
- 平台代码与外部题库数据分离
- 核心业务不绑定具体题库来源
- 避免为了技术栈展示而引入不必要的复杂组件
- 使用清晰的 Git Commit 记录项目演进过程

## License

本项目源代码的 License 将单独确定。

外部题库及学习资源遵循各自原始项目和内容权利方的授权及版权规则。
