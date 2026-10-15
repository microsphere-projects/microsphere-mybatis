# microsphere-mybatis-spring（1.x）—— 开发者文档

[English](../spring.md) · [← 1.x 索引](README.md) · [current 源码分析](../../current/zh/spring.md)

**与 `current` 无源码差异。** 注册器家族、注解属性 → BeanDefinition 映射、
`"*"`/空串的 bean 引用约定、`SqlSessionFactoryBeanPostProcessor` 全部相同 ——
完整分析见 [current/spring.md](../../current/zh/spring.md)。

## 版本线背景

| 方面 | 1.x |
|---|---|
| Spring 代际 | Spring Framework 5.x / mybatis-spring 2.1.2（current 为 6.x / 4.1.0） |
| API 兼容性 | 注册器面向 `SqlSessionFactoryBean` 与 `Configuration` 的 setter，这些在两条 mybatis-spring 版本线中都存在 —— 因此源码零差异 |
| `proxyFactory` 修复 | `Class`→实例 的修复随 0.1.16 发布，对应 current 线的 0.2.15 |

`@MyBatisConfiguration` 的属性列表映射到 MyBatis 3.5.x 起就稳定的 `Configuration` 方法，
因此同样的约 34 个属性在两条线上都适用。

## 另见

- [1.x 差异总览](README.md)
- [用户文档](../../../user-guide/1.x/zh/spring.md)
