# microsphere-mybatis-spring-test（1.x）—— 开发者文档

[English](../spring-test.md) · [← 1.x 索引](README.md) · [current 源码分析](../../current/zh/spring-test.md)

**与 `current` 无源码差异。** 三个 `@Configuration(proxyBeanMethods = false)` 类 ——
`MyBatisDataSourceTestConfiguration`（`@Bean(destroyMethod = "forceCloseAll")` + `@Primary`
DataSource）、`MyBatisDataBaseTestConfiguration`（上下文事件驱动建表/删表脚本）、
`MyBatisTestConfiguration`（手工调用 `SqlSessionFactoryBean.getObject()`）—— 全部相同。
细节分析见 [current/spring-test.md](../../current/zh/spring-test.md)。

## 版本线背景

- 表结构跟随**应用上下文**生命周期（而非 JUnit 扩展的每方法一次）的结论在本线同样成立。
- Spring 5.x 的 `@EventListener`/`ContextClosedEvent` 语义相同，因此本分支无需任何适配。

## 另见

- [1.x 差异总览](README.md)
- [用户文档](../../../user-guide/1.x/zh/spring-test.md)
