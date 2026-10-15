# 开发者文档 —— `current`（release / 0.2.x）

[English](../README.md) · [← 开发者文档总索引](../../README.md) · [用户文档（current）](../../../user-guide/current/zh/README.md)

针对 `release` 分支的源码分析：Java 17 基线、MyBatis 3.5.19、MyBatis-Spring 4.1.0、
Spring Boot 3 风格的 `AutoConfiguration.imports`。

## 阅读顺序

1. [architecture.md](architecture.md) —— 分层结构、模块依赖图、外部依赖策略，以及端到端拦截时序。
2. 之后按依赖顺序阅读各模块深度分析：

| # | 文档 | 模块 | 核心主题 |
|---|---|---|---|
| 1 | [utils.md](utils.md) | `microsphere-mybatis-utils` | `MyBatisUtils` 引导、`ScriptRunner`、`doIn*` 模板 |
| 2 | [core.md](core.md) | `microsphere-mybatis-core` | 过滤器链、`InterceptingExecutor`、适配器、`CachingExecutor` 解包 |
| 3 | [test.md](test.md) | `microsphere-mybatis-test` | JUnit 5 扩展生命周期、`ComponentResolver` SPI |
| 4 | [spring.md](spring.md) | `microsphere-mybatis-spring` | 注册器家族、注解 → Bean 属性映射 |
| 5 | [spring-test.md](spring-test.md) | `microsphere-mybatis-spring-test` | 事件驱动的数据库生命周期 |
| 6 | [spring-boot.md](spring-boot.md) | `microsphere-mybatis-spring-boot` | 自动配置条件与顺序 |
| 7 | [spring-cloud.md](spring-cloud.md) | `microsphere-mybatis-spring-cloud` | `features.yaml` → `HasFeatures` 绑定 |
| 8 | [parent-and-dependencies.md](parent-and-dependencies.md) | parent / BOM | POM 继承、`${revision}` flatten |

## 本目录文档约定

- 每篇深度分析包含：**架构设计**（对非直观流程提供 Mermaid 图）、**源码分析**（引用 `release`
  分支中的 `file:line`）、**设计模式**、**已知局限**，并附交叉链接。
- 所有结论均标注了验证所在的分支；1.x 分支存在差异处会提供指向 `../1.x/<module>.md` 的链接。
