# microsphere-mybatis-core — User Guide (1.x / 0.1.x)

[中文](zh/core.md) · [Developer Guide](../../developer-guide/1.x/core.md) · [← 1.x Index](README.md)

The `ExecutorFilter` / `ExecutorInterceptor` chain behaves identically in 0.1.x;
read [current/core.md](../current/core.md) for the complete API reference, examples and
semantics. 1.x-specific facts are below.

## Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-mybatis-core` |
| Documented version | `0.1.16` (branch `release-1.x`, 0.1.x line) |
| Java baseline | **8** |
| Requires | `microsphere-mybatis-utils` 0.1.16; MyBatis `3.5.19` |
| Compatibility | Any MyBatis 3.5.x under javax-era stacks (Spring 5, Spring Boot 2) |

## API Differences vs `current`

**None at the public API level.** Classes `ExecutorFilter`, `ExecutorInterceptor`,
`ExecutorFilterChain`, `InterceptingExecutor`, `InterceptorsExecutorFilterAdapter`,
`InterceptingExecutorInterceptor`, `InterceptorContext`, `Executors`, `Plugins`,
`LoggingExecutorFilter`, `LoggingExecutorInterceptor`, `PropertyConstants` exist with identical
signatures. Internally, 0.1.x uses classic `instanceof` casts (instead of Java 16+ pattern
matching) and MyBatis' `org.apache.ibatis.util.MapUtil.computeIfAbsent` inside `Plugins` —
observable behaviour is unchanged.

## Usage Recap (Java 8 compatible)

```java
Configuration configuration =
    MyBatisUtils.getConfiguration("META-INF/mybatis/config.xml", "testing");

configuration.addInterceptor(new InterceptingExecutorInterceptor(
        new ExecutorFilter[]{new LoggingExecutorFilter()},
        new ExecutorInterceptor[]{new LoggingExecutorInterceptor()}));

try (SqlSession session = MyBatisUtils.buildSqlSessionFactory(configuration).openSession()) {
    UserMapper mapper = session.getMapper(UserMapper.class);
    // every mapper call passes through the filter chain
}
```

`microsphere.mybatis.enabled` (`PropertyConstants.MICROSPHERE_MYBATIS_ENABLED_PROPERTY_NAME`)
also exists in the 0.1.x line and gates the Spring Boot/Cloud auto-configurations.

## See Also

- [Full API reference (current)](../current/core.md) — tables of every method and callback
- [1.x spring-boot](spring-boot.md) — `spring.factories` registration differences
