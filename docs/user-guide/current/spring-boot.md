# microsphere-mybatis-spring-boot — User Guide

[中文](zh/spring-boot.md) · [Developer Guide](../../developer-guide/current/spring-boot.md) · [← Index](../README.md)

Spring Boot auto-configuration that applies the `@EnableMyBatisExtension` behaviour
automatically: any `ExecutorFilter` / `ExecutorInterceptor` bean (or SPI-declared implementation)
in your application is wired into the MyBatis `SqlSessionFactory` — with no MyBatis config edits.

## Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-mybatis-spring-boot` |
| Documented version | `0.2.16` (branch `release`, 0.2.x line) |
| Java baseline | 17 |
| Requires | `microsphere-mybatis-spring`; optional: `mybatis-spring-boot-starter` `3.0.5`, Spring Boot 3.x |
| Registration | `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (Spring Boot 2.7+ mechanism — **not** `spring.factories`; the 0.1.x line uses `spring.factories`, see [1.x guide](../1.x/spring-boot.md)) |
| Ordering | `@AutoConfigureAfter(name = "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration")` — the official starter builds `sqlSessionFactory`/`sqlSessionTemplate`; this module only extends them |

## Installation

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-mybatis-spring-boot</artifactId>
</dependency>
<!-- you still add the official starter yourself -->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
</dependency>
```

## Configuration Properties

| Property | Type | Default | Description |
|---|---|---|---|
| `microsphere.mybatis.enabled` | `boolean` | `true` | Set to `false` to disable `MyBatisAutoConfiguration` entirely |

Auto-configuration conditions (`@ConditionalOnMyBatisAvailable`):

1. `@ConditionalOnMyBatisEnabled` — property check above (`matchIfMissing = true`).
2. `@ConditionalOnClass` — `org.apache.ibatis.session.SqlSession` and `SqlSessionFactory` on the classpath.

## How to Participate

Declare `ExecutorFilter` / `ExecutorInterceptor` beans — that is the entire API:

```java
@SpringBootApplication
public class MyApplication {

    @Bean
    public ExecutorFilter metricsExecutorFilter() {
        return new MetricsExecutorFilter();
    }

    @Bean
    public ExecutorInterceptor auditExecutorInterceptor() {
        return new AuditExecutorInterceptor();
    }
}
```

Registered bean names to be aware of (from `MyBatisExtensionBeanDefinitionRegistrar`):
`interceptingExecutorInterceptor` (the MyBatis plugin), plus one bean per discovered
filter/interceptor (e.g. `loggingExecutorFilter`).

## Test-Case Examples

Existence test (`MyBatisAutoConfigurationIntegrationTest`):

```java
@SpringBootTest(classes = {
        LoggingExecutorFilter.class,
        LoggingExecutorInterceptor.class,
        MyBatisAutoConfigurationIntegrationTest.class
})
@EnableAutoConfiguration
class MyBatisAutoConfigurationIntegrationTest {

    @Autowired @Qualifier(SQL_SESSION_FACTORY_BEAN_NAME)
    private SqlSessionFactory sqlSessionFactory;

    @Autowired @Qualifier(SQL_SESSION_TEMPLATE_BEAN_NAME)
    private SqlSessionTemplate sqlSessionTemplate;

    @Autowired @Qualifier(INTERCEPTING_EXECUTOR_INTERCEPTOR_BEAN_NAME)
    private InterceptingExecutorInterceptor interceptor;   // proves the plugin was registered
}
```

Disabling test (`ConditionalOnMyBatisEnabledTest`):

```java
@SpringBootTest(classes = EnabledConfig.class, properties = "microsphere.mybatis.enabled = false")
class ConditionalOnMyBatisEnabledTest { /* asserts the configuration bean is absent */ }
```

Auto-configuration contract test (`MyBatisAutoConfigurationTest`) extends
`AutoConfigurationTest<MyBatisAutoConfiguration>` and verifies three scenarios: the class is
auto-configured; the global disable property removes it; missing MyBatis classes remove it.

## See Also

- [core module](core.md) — what filters/interceptors can do
- [spring module](spring.md) — the non-Boot equivalent (`@EnableMyBatisExtension`)
- [Developer Guide: spring-boot](../../developer-guide/current/spring-boot.md)
