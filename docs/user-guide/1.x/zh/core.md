# microsphere-mybatis-core —— 用户文档（1.x / 0.1.x）

[English](../core.md) · [开发者文档](../../../developer-guide/1.x/zh/core.md) · [← 1.x 索引](README.md)

`ExecutorFilter` / `ExecutorInterceptor` 链在 0.1.x 中行为与 0.2.x 完全一致，
完整 API 参考、示例与语义请阅读 [current/zh/core.md](../../current/zh/core.md)。

## 版本信息

| 项目 | 值 |
|---|---|
| 构件 | `io.github.microsphere-projects:microsphere-mybatis-core` |
| 文档对应版本 | `0.1.16`（`release-1.x` 分支，0.1.x 线） |
| Java 基线 | **8** |
| 依赖 | `microsphere-mybatis-utils` 0.1.16；MyBatis `3.5.19` |
| 兼容性 | javax 时代技术栈（Spring 5、Spring Boot 2）上的任意 MyBatis 3.5.x |

## 与 `current` 的 API 差异

**公开 API 层面没有任何差异。** `ExecutorFilter`、`ExecutorInterceptor`、`ExecutorFilterChain`、
`InterceptingExecutor`、`InterceptorsExecutorFilterAdapter`、`InterceptingExecutorInterceptor`、
`InterceptorContext`、`Executors`、`Plugins`、`LoggingExecutorFilter`、`LoggingExecutorInterceptor`、
`PropertyConstants` 签名完全相同。内部实现上，0.1.x 使用传统 `instanceof` 强转（而非 Java 16+
模式匹配），`Plugins` 内使用 MyBatis 的 `org.apache.ibatis.util.MapUtil.computeIfAbsent`，
可观察行为不变。

## 用法回顾（Java 8 兼容）

```java
Configuration configuration =
    MyBatisUtils.getConfiguration("META-INF/mybatis/config.xml", "testing");

configuration.addInterceptor(new InterceptingExecutorInterceptor(
        new ExecutorFilter[]{new LoggingExecutorFilter()},
        new ExecutorInterceptor[]{new LoggingExecutorInterceptor()}));

try (SqlSession session = MyBatisUtils.buildSqlSessionFactory(configuration).openSession()) {
    UserMapper mapper = session.getMapper(UserMapper.class);
    // 每次 Mapper 调用都会穿过过滤器链
}
```

配置属性 `microsphere.mybatis.enabled` 在 0.1.x 中同样存在，
用于门控 Spring Boot / Cloud 自动配置。

## 另见

- [完整 API 参考（current）](../../current/zh/core.md)
- [1.x spring-boot](spring-boot.md) —— 本线的 `spring.factories` 注册差异
