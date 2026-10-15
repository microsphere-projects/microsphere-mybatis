# microsphere-mybatis-spring-cloud（1.x）—— 开发者文档

[English](../spring-cloud.md) · [← 1.x 索引](README.md) · [current 源码分析](../../current/zh/spring-cloud.md)

依旧是那个空的 `MyBatisCloudAutoConfiguration`（`@ConditionalOnMyBatisAvailable` +
`@AutoConfigureAfter(name=…)`），`META-INF/config/default/features.yaml` 在两个分支上内容
完全相同（四个 `abstract` 分组 + 一个 `named` 条目；详见
[current/spring-cloud.md](../../current/zh/spring-cloud.md)）。只有发现机制的注册方式不同。

## 注册差异

与 boot 模块一样，1.x 同时携带两个文件：

```
META-INF/spring.factories
    org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
    io.microsphere.mybatis.spring.cloud.autoconfigure.MyBatisCloudAutoConfiguration

META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
    io.microsphere.mybatis.spring.cloud.autoconfigure.MyBatisCloudAutoConfiguration
```

因此 `features.yaml` → `HasFeatures` 的绑定在 Spring Cloud Hoxton – 2021.0.x 上同样生效
（0.1.26 代际的 microsphere-spring-cloud 提供与 current 相同的特性加载器）。

## POM 已知问题（两条线相同）

本模块的 `microsphere-mybatis-spring-test` 依赖同样**没有声明
`<scope>test</scope>`** —— 在 `release-1.x` 上也会作为 compile 作用域的传递依赖泄漏。
两条线的消费方都应自行 exclude。

## 另见

- [spring-boot 1.x](spring-boot.md) —— 相同双重注册的动机
- [1.x 差异总览](README.md)
- [用户文档](../../../user-guide/1.x/zh/spring-cloud.md)
