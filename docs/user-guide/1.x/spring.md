# microsphere-mybatis-spring — User Guide (1.x / 0.1.x)

[中文](zh/spring.md) · [Developer Guide](../../developer-guide/1.x/spring.md) · [← 1.x Index](README.md)

`@EnableMyBatis`, `@MyBatisConfiguration` and `@EnableMyBatisExtension` work exactly as in the
0.2.x line — the full attribute tables and examples live at
[current/spring.md](../current/spring.md). 1.x specifics:

## Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-mybatis-spring` |
| Documented version | `0.1.16` (branch `release-1.x`, 0.1.x line) |
| Java baseline | **8** |
| Requires | `microsphere-mybatis-core` 0.1.16; MyBatis-Spring **2.1.2**, Spring Framework **5.x** (javax), `microsphere-spring-context` 0.1.26 — all optional, supply them in your app |

## 1.x Notes

- Targets the **javax** namespace generation: built against mybatis-spring 2.1.2 and Spring 5.
  On Spring 6 / Boot 3 use the [current line](../current/spring.md) instead.
- `@MyBatisConfiguration` in 0.1.x exposes the same attribute set that maps to MyBatis 3.5.19's
  `Configuration`; note that `proxyFactory` is instantiated via the MyBatis
  `ProxyFactory` API available in that line.
- `@EnableMyBatisExtension(sources = ...)` discovers `ExecutorFilter` / `ExecutorInterceptor`
  implementations from `BeanSource` selections — the `SPRING_FACTORIES` source is especially
  relevant on this line since Spring 5 itself uses `META-INF/spring.factories`.
- Both branch lines implement the same registrars and semantics; when in doubt, treat the
  `current` documentation as authoritative and check only the version table above.

## Usage Recap

```java
@Configuration
@EnableMyBatis(configLocation = "classpath:META-INF/mybatis/config.xml", checkConfigLocation = true)
@EnableMyBatisExtension
@Import({LoggingExecutorFilter.class, LoggingExecutorInterceptor.class})
public class MyBatisConfig {
}
```

Produces beans `sqlSessionFactory`, `sqlSessionTemplate`, `configuration` (if no configLocation),
`interceptingExecutorInterceptor` plus the `SqlSessionFactoryBeanPostProcessor` that installs
the plugin — identical names and wiring as `current`.

## See Also

- [Attribute tables (current)](../current/spring.md)
- [1.x spring-boot](spring-boot.md) — how the same registrar is triggered automatically here
