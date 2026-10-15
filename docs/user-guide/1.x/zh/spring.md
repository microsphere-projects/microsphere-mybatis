# microsphere-mybatis-spring —— 用户文档（1.x / 0.1.x）

[English](../spring.md) · [开发者文档](../../../developer-guide/1.x/zh/spring.md) · [← 1.x 索引](README.md)

`@EnableMyBatis`、`@MyBatisConfiguration`、`@EnableMyBatisExtension` 与 0.2.x 线行为一致，
完整属性表与示例见 [current/zh/spring.md](../../current/zh/spring.md)。1.x 特有信息：

## 版本信息

| 项目 | 值 |
|---|---|
| 构件 | `io.github.microsphere-projects:microsphere-mybatis-spring` |
| 文档对应版本 | `0.1.16`（`release-1.x` 分支，0.1.x 线） |
| Java 基线 | **8** |
| 依赖 | `microsphere-mybatis-core` 0.1.16；MyBatis-Spring **2.1.2**、Spring Framework **5.x**（javax）、`microsphere-spring-context` 0.1.26（均为 optional，应用侧提供） |

## 1.x 说明

- 面向 **javax** 代际：编译于 mybatis-spring 2.1.2 与 Spring 5。
  Spring 6 / Boot 3 环境请改用 [current 线](../../current/zh/spring.md)。
- `@EnableMyBatisExtension(sources = ...)` 的 `SPRING_FACTORIES` 来源在本线尤其顺手 ——
  Spring 5 自身就使用 `META-INF/spring.factories`。
- 两条分支线实现相同的注册器与语义；有疑问时以 `current` 文档为准，并核对上方版本表。

## 用法回顾

```java
@Configuration
@EnableMyBatis(configLocation = "classpath:META-INF/mybatis/config.xml", checkConfigLocation = true)
@EnableMyBatisExtension
@Import({LoggingExecutorFilter.class, LoggingExecutorInterceptor.class})
public class MyBatisConfig {
}
```

产出 Bean `sqlSessionFactory`、`sqlSessionTemplate`、`configuration`（未提供 configLocation 时）、
`interceptingExecutorInterceptor`，以及把插件安装进 `SqlSessionFactoryBean` 的
`SqlSessionFactoryBeanPostProcessor` —— 名称与装配方式与 `current` 相同。

## 另见

- [属性表（current）](../../current/zh/spring.md)
- [1.x spring-boot](spring-boot.md) —— 本线如何由自动配置触发同样的注册器
