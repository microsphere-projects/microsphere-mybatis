# Microsphere MyBatis 文档

欢迎阅读 **Microsphere MyBatis** 文档。本项目为 MyBatis 提供了 Executor 级别的过滤器/拦截器链，
并提供 Spring、Spring Boot、Spring Cloud 与 JUnit 5 的一流集成。

English version: [Documentation Home](../README.md)

## 文档入口

| 文档 | 说明 |
|---|---|
| [用户文档](../user-guide/zh/README.md) | 各 Maven 模块的安装、API、配置项与代码示例 |
| [开发者文档](../developer-guide/zh/README.md) | 架构设计、源码分析、设计模式与流程图 |

## 源码差异

Tag 对 tag 的源码对比：每次对比一对文件，`src/main` 与 `src/test` 各一篇。对比时已过滤 Java
`8` ↔ `17` 的语法/API 差异以及 Java EE ↔ Jakarta EE 的 API 差异，因此表格只保留真正的实现分歧
（被排除项仍单独列出以便追溯）。

| 对比组 | `src/main` | `src/test` | 覆盖范围 |
|---|---|---|---|
| `0.2.16` vs `0.1.16` | [main-diff-0.2.16-vs-0.1.16.zh.md](../diff/source/main-diff-0.2.16-vs-0.1.16.zh.md) · [English](../diff/source/main-diff-0.2.16-vs-0.1.16.md) | [test-diff-0.2.16-vs-0.1.16.zh.md](../diff/source/test-diff-0.2.16-vs-0.1.16.zh.md) · [English](../diff/source/test-diff-0.2.16-vs-0.1.16.md) | 两条发布线各自最新的 tag；9 个 Maven 模块全部按名称匹配 |

结论概览：过滤后 `src/main` 保留 3 处差异（`Plugins.java` 的一处 JDK `Map` API 替换，加上仅由 0.1.x
承载的两处传统 `spring.factories` 注册），`src/test` 保留 0 处。

## 收录分支与版本

文档按发布分支线组织，每个指南目录下包含对应分支的子目录：

| 目录 | Git 分支 | 版本线 | 最新发布版本 | 目标平台 |
|---|---|---|---|---|
| `current` | [`release`](https://github.com/microsphere-projects/microsphere-mybatis/tree/release) | 0.2.x | 0.2.16 | Java 17+ · Spring Boot 3.x · Spring Cloud 2022.0.x – 2025.0.x |
| `1.x` | [`release-1.x`](https://github.com/microsphere-projects/microsphere-mybatis/tree/release-1.x) | 0.1.x | 0.1.16 | Java 8+ · Spring Boot 2.x · Spring Cloud Hoxton – 2021.0.x |

两个分支源码在功能上等价，差异仅在于：

1. **依赖代际** —— `current` 编译于 MyBatis-Spring `4.1.0` 与 mybatis-spring-boot-starter `3.0.5`；
   `1.x` 编译于 MyBatis-Spring `2.1.2` 与 mybatis-spring-boot-starter `2.1.2`（两者均使用 MyBatis `3.5.19`）。
2. **自动配置注册方式** —— 两条线都通过
   `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册自动配置；
   `1.x` 额外保留传统的 `META-INF/spring.factories`，以便 2.7 以下、无法读取 `.imports` 文件的
   Spring Boot 使用方仍能发现自动配置。详见
   [main-diff-0.2.16-vs-0.1.16.zh.md](../diff/source/main-diff-0.2.16-vs-0.1.16.zh.md)。
3. **Java 基线** —— `current` 要求 Java 17（使用了 `instanceof` 模式匹配）；`1.x` 面向 Java 8 构建，
   CI 在 Java 8/11/17/21/25 上验证。

## 目录结构

```
docs/
├── README.md                     # 英文文档首页
├── zh/README.md                  # 本文件
├── user-guide/                   # 用户文档（英文默认，zh/ 子目录存放中文）
│   ├── current/                  # 0.2.x 各模块文档
│   └── 1.x/                      # 0.1.x 各模块文档
├── developer-guide/              # 开发者文档（结构同上）
│   ├── current/                  # 含 architecture.md 总体架构与模块依赖图
│   └── 1.x/
└── diff/
    └── source/                   # tag 对 tag 的源码对比
        ├── main-diff-0.2.16-vs-0.1.16.md    # src/main 对比（英文）
        ├── main-diff-0.2.16-vs-0.1.16.zh.md # src/main 对比（中文）
        ├── test-diff-0.2.16-vs-0.1.16.md    # src/test 对比（英文）
        └── test-diff-0.2.16-vs-0.1.16.zh.md # src/test 对比（中文）
```

## 许可

全部文档基于 [Apache License, Version 2.0](../../LICENSE) 发布。
