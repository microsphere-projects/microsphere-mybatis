# microsphere-mybatis-spring-boot —— 用户文档（1.x / 0.1.x）

[English](../spring-boot.md) · [开发者文档](../../../developer-guide/1.x/zh/spring-boot.md) · [← 1.x 索引](README.md)

面向 **Spring Boot 2.x** 的 Executor 扩展自动配置。功能行为与 0.2.x 线一致
（见 [current/zh/spring-boot.md](../../current/zh/spring-boot.md)），差异在注册方式与依赖代际。

## 版本信息

| 项目 | 值 |
|---|---|
| 构件 | `io.github.microsphere-projects:microsphere-mybatis-spring-boot` |
| 文档对应版本 | `0.1.16`（`release-1.x` 分支，0.1.x 线） |
| Java 基线 | **8** |
| 依赖 | `microsphere-mybatis-spring` 0.1.16；mybatis-spring-boot-starter **2.1.2**、Spring Boot **2.x**（optional） |
| 兼容性 | Spring Boot 2.1 – 2.7 时代；Spring Cloud Hoxton – 2021.0.x |

## 注册方式 —— 传统 `spring.factories`

本线**不**提供 `AutoConfiguration.imports`，`META-INF/spring.factories` 内容为：

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.microsphere.mybatis.spring.boot.autoconfigure.MyBatisAutoConfiguration
```

- Spring Boot 2.7+ 同时识别两种注册文件；2.6 及以下仅识别本文件。
- 之后升级到 0.2.x 线时注册文件自动切换，应用侧无需修改。

## 类定义（与 current 相同的注解组合）

```java
@ConditionalOnMyBatisAvailable
@EnableMyBatisExtension
@AutoConfigureAfter(name = {
        "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration"})
public class MyBatisAutoConfiguration { }
```

类体为空，配置类身份来自 `@EnableMyBatisExtension` 的元 `@Import`；
两条版本线都不依赖 `@Configuration`/`@AutoConfiguration` 注解。

## 属性与条件（与 current 相同）

| 属性 | 默认值 | 效果 |
|---|---|---|
| `microsphere.mybatis.enabled` | `true` | `false` 时禁用自动配置（`@ConditionalOnMyBatisEnabled`，`matchIfMissing = true`） |

`@ConditionalOnMyBatisAvailable` 另要求 classpath 存在 `SqlSession` 与 `SqlSessionFactory`。

## 用法

与 `current` 相同：声明 `ExecutorFilter` / `ExecutorInterceptor` Bean 即可。

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-mybatis-spring-boot</artifactId>
    <version>0.1.16</version>
</dependency>
```

## 另见

- [current/zh/spring-boot.md](../../current/zh/spring-boot.md) —— 完整 API 与测试用例
- [1.x spring-cloud](spring-cloud.md)
