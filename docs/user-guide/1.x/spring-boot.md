# microsphere-mybatis-spring-boot — User Guide (1.x / 0.1.x)

[中文](zh/spring-boot.md) · [Developer Guide](../../developer-guide/1.x/spring-boot.md) · [← 1.x Index](README.md)

Auto-configuration for the Executor extension on **Spring Boot 2.x**. Functional behaviour
equals the 0.2.x line ([current/spring-boot.md](../current/spring-boot.md)); the registration
and dependency story differs.

## Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-mybatis-spring-boot` |
| Documented version | `0.1.16` (branch `release-1.x`, 0.1.x line) |
| Java baseline | **8** |
| Requires | `microsphere-mybatis-spring` 0.1.16; mybatis-spring-boot-starter **2.1.2**, Spring Boot **2.x** (optional — supply in app) |
| Compatibility | Spring Boot 2.1 – 2.7 era; Spring Cloud Hoxton – 2021.0.x |

## Registration — legacy `spring.factories`

`META-INF/spring.factories` (this line does **not** ship `AutoConfiguration.imports`):

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.microsphere.mybatis.spring.boot.autoconfigure.MyBatisAutoConfiguration
```

Consequences:

- On Spring Boot 2.7+, both registration styles are honoured; on ≤2.6 only this file works.
- If you later migrate to the 0.2.x line, nothing to do — registration moves automatically.

## Class Differences vs `current`

```java
@ConditionalOnMyBatisAvailable
@EnableMyBatisExtension
@AutoConfigureAfter(name = {
        "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration"})
public class MyBatisAutoConfiguration { }   // annotation set identical to current
```

The class relies on `@EnableMyBatisExtension`'s meta-`@Import` to become a configuration
candidate — no `@Configuration`/`@AutoConfiguration` annotation is present on either line;
`spring.factories` (Boot 2) and `AutoConfiguration.imports` (Boot 2.7+/3) each honour that.

## Properties & Conditions (identical to `current`)

| Property | Default | Effect |
|---|---|---|
| `microsphere.mybatis.enabled` | `true` | `false` disables the auto-configuration (`@ConditionalOnMyBatisEnabled`, `matchIfMissing = true`) |

`@ConditionalOnMyBatisAvailable` additionally requires `org.apache.ibatis.session.SqlSession`
and `SqlSessionFactory` on the classpath.

## Usage

Same as `current`: declare `ExecutorFilter` / `ExecutorInterceptor` beans, optionally add
`microsphere-mybatis-spring-cloud` for feature reporting.

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-mybatis-spring-boot</artifactId>
    <version>0.1.16</version>
</dependency>
```

## See Also

- [current/spring-boot.md](../current/spring-boot.md) — full API and test-case examples
- [1.x spring-cloud](spring-cloud.md)
