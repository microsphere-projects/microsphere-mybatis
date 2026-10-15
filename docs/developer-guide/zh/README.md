# 开发者文档索引

开发者文档讲解 Microsphere MyBatis 的实现方式：架构设计、每个 Maven 模块的源码分析、
所使用的设计模式，以及关键执行路径的 Mermaid 流程图。

English: [Developer Guide index](../README.md)

## 选择版本线

| 目录 | 分支 | 版本线 |
|---|---|---|
| [`current/`](../current/zh/architecture.md) | `release` | 0.2.x（Java 17、MyBatis-Spring 4.1.0、`AutoConfiguration.imports`） |
| [`1.x/`](../1.x/zh/architecture.md) | `release-1.x` | 0.1.x（Java 8、MyBatis-Spring 2.1.2、`spring.factories`） |

两条版本线的实现几乎一致，`1.x/` 文档重点记录差异。

## 建议阅读顺序

1. [总体架构与模块依赖图（current）](../current/zh/architecture.md) —— 分层结构、依赖流向、
   外部依赖策略与请求拦截全链路总览。
2. [总体架构与模块依赖图（1.x）](../1.x/zh/architecture.md)。

## 模块深度解析

| 模块 | 文档（current） | 文档（1.x） | 核心主题 |
|---|---|---|---|
| `microsphere-mybatis-utils` | [utils.md](../current/zh/utils.md) | [utils.md](../1.x/zh/utils.md) | Configuration 引导、基于资源的 `ScriptRunner` |
| `microsphere-mybatis-core` | [core.md](../current/zh/core.md) | [core.md](../1.x/zh/core.md) | 过滤器链、拦截器适配器、插件桥接、`CachingExecutor` 解包 |
| `microsphere-mybatis-test` | [test.md](../current/zh/test.md) | [test.md](../1.x/zh/test.md) | JUnit 5 扩展生命周期、基于 `ServiceLoader` 的解析器 SPI |
| `microsphere-mybatis-spring` | [spring.md](../current/zh/spring.md) | [spring.md](../1.x/zh/spring.md) | `ImportBeanDefinitionRegistrar` 家族、注解属性 → BeanProperty 映射 |
| `microsphere-mybatis-spring-test` | [spring-test.md](../current/zh/spring-test.md) | [spring-test.md](../1.x/zh/spring-test.md) | 事件驱动的数据库生命周期 |
| `microsphere-mybatis-spring-boot` | [spring-boot.md](../current/zh/spring-boot.md) | [spring-boot.md](../1.x/zh/spring-boot.md) | 自动配置排序与条件装配 |
| `microsphere-mybatis-spring-cloud` | [spring-cloud.md](../current/zh/spring-cloud.md) | [spring-cloud.md](../1.x/zh/spring-cloud.md) | 特性目录绑定（`features.yaml`） |
| `parent` / `dependencies` | [parent-and-dependencies.md](../current/zh/parent-and-dependencies.md) | [parent-and-dependencies.md](../1.x/zh/parent-and-dependencies.md) | POM 继承与 BOM 导入、`${revision}` flatten |

## 所用设计模式索引

| 模式 | 位置 |
|---|---|
| 责任链 | `ExecutorFilterChain.process(...)` 通过 `position` 推进 `filters[]` |
| 装饰器 | `InterceptingExecutor` 包装真实 `Executor` |
| 适配器 | `InterceptorsExecutorFilterAdapter` 将 `ExecutorInterceptor[]` 适配为单个 `ExecutorFilter` |
| 观察者 / 回调 | `ExecutorInterceptor` 的 `beforeXxx` / `afterXxx` 成对回调 |
| 模板方法 | `AbstractMyBatisTest` 继承体系、`AbstractComponentResolver.doResolve(...)` |
| 策略 | `ComponentResolver` 各实现通过 `supportsParameter`/`supportsField` 匹配 |
| 服务提供者（SPI） | `ComponentResolver` 的 `ServiceLoader` 注册 |
| 静态工厂 / 工具类 | `MyBatisUtils`、`Executors`、`Plugins`（抽象类 + 私有构造器） |
| 注册器家族 | `MyBatisImportBeanDefinitionRegistrar` 基类 + 三个具体注册器 |
| Bean 后置处理器 | `SqlSessionFactoryBeanPostProcessor` 注入插件 |

## 构建与测试

- 构建：`./mvnw build`（Windows 使用 `mvnw.cmd build`）。
- CI 矩阵（current 线）：Java 17 / 21 / 25 × Spring Cloud profile `2022`–`2025`；
  1.x 线额外验证 Java 8、11 与 Spring Cloud `hoxton`–`2021`。
- 测试基于内存 H2（`jdbc:h2:mem:test_mem`），无需外部数据库。
