# microsphere-mybatis-spring-cloud — User Guide (1.x / 0.1.x)

[中文](zh/spring-cloud.md) · [Developer Guide](../../developer-guide/1.x/spring-cloud.md) · [← 1.x Index](README.md)

Feature-catalogue integration for **Spring Cloud Hoxton – 2021.0.x** (Spring Cloud Contract
`HasFeatures` + Actuator). Behaviour matches [current/spring-cloud.md](../current/spring-cloud.md).

## Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-mybatis-spring-cloud` |
| Documented version | `0.1.16` (branch `release-1.x`, 0.1.x line) |
| Java baseline | **8** |
| Requires | `microsphere-mybatis-spring-boot` 0.1.16; optional: `spring-cloud-commons` (2.x), `spring-boot-starter-actuator`, `microsphere-spring-cloud-commons` 0.1.26 |
| Registration | `META-INF/spring.factories` entry `EnableAutoConfiguration=io.microsphere.mybatis.spring.cloud.autoconfigure.MyBatisCloudAutoConfiguration` |

## Registration — legacy `spring.factories`

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.microsphere.mybatis.spring.cloud.autoconfigure.MyBatisCloudAutoConfiguration
```

## What You Get

The same `META-INF/config/default/features.yaml` catalogue as `current`:
`mybatis.features` (19 MyBatis SPI types), `mybatis-spring.features`,
`mybatis-spring-boot-autoconfigure.features`, `microsphere-mybatis-core.features`
(`ExecutorFilter`, `ExecutorInterceptor`).

Enable with Boot 2 properties:

```properties
spring.cloud.features.enabled=true
management.endpoints.web.exposure.include=*
```

`@ConditionalOnMyBatisAvailable` and `microsphere.mybatis.enabled=false` behave as on `current`.

## See Also

- [current/spring-cloud.md](../current/spring-cloud.md) — integration-test example & full table
