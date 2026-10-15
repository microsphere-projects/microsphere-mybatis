# microsphere-mybatis-spring-test (1.x) — Developer Guide

[中文](zh/spring-test.md) · [← 1.x Index](README.md) · [Current analysis](../current/spring-test.md)

**No source differences vs `current`.** All three `@Configuration(proxyBeanMethods = false)`
classes — `MyBatisDataSourceTestConfiguration` (`@Bean(destroyMethod = "forceCloseAll")`
`@Primary` DataSource), `MyBatisDataBaseTestConfiguration` (context-event DB scripts),
`MyBatisTestConfiguration` (manual `SqlSessionFactoryBean.getObject()`) — are identical.
See [current/spring-test.md](../current/spring-test.md) for the quirks analysis.

## Line context

- The per-application-context schema lifecycle (vs the JUnit extension's per-method one)
  applies identically here.
- Spring 5.x provides `@EventListener`/`ContextClosedEvent` with the same semantics, so no
  adaptation was needed on this branch.

## See Also

- [1.x differences overview](README.md)
- [User Guide](../../user-guide/1.x/spring-test.md)
