# IELTS Learning Platform

一个基于 **Vue 3、Spring Boot 和 SQLite** 从零设计与开发的跨平台 IELTS 学习平台。

项目目标是构建一个结构清晰、可维护、可扩展的 IELTS 学习系统，逐步支持阅读练习、词汇复习、写作练习、学习记录、数据统计、离线学习以及后续的 AI 辅助学习能力。

## 项目目标

- 使用 Java + Spring Boot 构建核心后端
- 支持 Web 和 Windows 桌面端
- 使用 SQLite 实现本地离线学习
- 将平台业务逻辑与外部 IELTS 题库数据解耦
- 记录学习历史并提供学习统计
- 后续接入 AI 写作评分与学习辅助能力

## 当前架构

```text
Vue 3
  |
REST API
  |
Spring Boot
  |
SQLite
```

后续桌面端计划：

```text
Tauri
  |
Vue 3
  |
Spring Boot
  |
SQLite
```

AI 相关能力将在后续阶段通过独立的 AI 服务层接入。

## 项目结构

```text
IELTS-Learning-Platform/
├── frontend/
├── backend/
├── desktop/
├── ai-runtime/
├── tools/
├── docs/
└── data/
```

## 题库与数据说明

本项目中的以下内容由我独立设计和开发：

- 系统架构
- 前端页面与交互
- Spring Boot 后端
- 数据库结构
- REST API
- 学习业务逻辑
- 学习记录与统计
- 外部题库导入机制

IELTS 练习题库可以从外部数据源导入。

当前计划用于本地开发和测试的外部数据来源之一：

- IELTS Atlas
  https://github.com/sallowayma-git/IELTS-practice

外部来源中的 IELTS 文章、题目、答案、音频、PDF、图片及其他学习资源并非由本项目创作，其版权和使用规则仍归原始权利方所有。

本项目计划通过题库导入器将外部数据转换为平台内部统一的数据格式，而不是让核心业务直接依赖某一个具体题库的数据结构。

大型外部题库资源不会直接提交到本仓库。

## 当前进度

**Phase 0：项目初始化**

- [x] 创建独立 Git 仓库
- [x] 初始化项目目录结构
- [x] 建立独立 Git 提交历史
- [ ] 编写第一版架构文档
- [ ] 初始化 Spring Boot 后端
- [ ] 初始化 Vue 3 前端
- [ ] 接入 SQLite
- [ ] 设计 Reading 统一题库格式
- [ ] 实现题库导入器

## 开发路线

1. 初始化 Spring Boot 后端
2. 接入 SQLite 和 Flyway
3. 初始化 Vue 3 前端
4. 建立统一 REST API 调用层
5. 设计 Reading 内部数据模型
6. 实现外部 IELTS 题库导入器
7. 完成 Reading MVP
8. 增加学习历史和统计
9. 增加 Vocabulary 模块
10. 增加 Writing 与 AI 辅助功能
11. 增加 Tauri Windows 桌面端

## 项目原则

- 平台代码与外部题库数据分离
- 核心业务逻辑不直接依赖具体题库来源
- 每个阶段保持项目可运行
- 优先完成完整业务闭环，再增加复杂功能
- 通过 Git Commit、Issue、文档和版本 Tag 记录项目演进过程

## License

本项目源代码的 License 将单独确定。

外部题库及学习资源遵循各自原始项目和内容权利方的授权及版权规则。
