# microsphere-mybatis-spring-cloud —— 用户文档（1.x / 0.1.x）

[English](../spring-cloud.md) · [开发者文档](../../../developer-guide/1.x/zh/spring-cloud.md) · [← 1.x 索引](README.md)

面向 **Spring Cloud Hoxton – 2021.0.x**（Spring Cloud Contract `HasFeatures` + Actuator）
的特性目录集成。行为与 [current/zh/spring-cloud.md](../../current/zh/spring-cloud.md) 一致。

## 版本信息

| 项目 | 值 |
|---|---|
| 构件 | `io.github.microsphere-projects:microsphere-mybatis-spring-cloud` |
| 文档对应版本 | `0.1.16`（`release-1.x` 分支，0.1.x 线） |
| Java 基线 | **8** |
| 依赖 | `microsphere-mybatis-spring-boot` 0.1.16；optional：`spring-cloud-commons`（2.x）、`spring-boot-starter-actuator`、`microsphere-spring-cloud-commons` 0.1.26 |
| 注册方式 | `META-INF/spring.factories`：`EnableAutoConfiguration=io.microsphere.mybatis.spring.cloud.autoconfigure.MyBatisCloudAutoConfiguration` |

## 注册 —— 传统 `spring.factories`

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.microsphere.mybatis.spring.cloud.autoconfigure.MyBatisCloudAutoConfiguration
```

## 提供内容

与 `current` 相同的 `META-INF/config/default/features.yaml` 目录：
`mybatis.features`（19 个 MyBatis SPI 类型）、`mybatis-spring.features`、
`mybatis-spring-boot-autoconfigure.features`、`microsphere-mybatis-core.features`
（`ExecutorFilter`、`ExecutorInterceptor`）。

Boot 2 属性开启：

```properties
spring.cloud.features.enabled=true
management.endpoints.web.exposure.include=*
```

`@ConditionalOnMyBatisAvailable` 与 `microsphere.mybatis.enabled=false` 行为与 `current` 相同。

## 另见

- [current/zh/spring-cloud.md](../../current/zh/spring-cloud.md) —— 集成测试示例与完整表格
