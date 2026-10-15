# 用户文档索引

用户文档介绍如何安装与使用 Microsphere MyBatis 的每个 Maven 模块：Maven 坐标、API 说明、
配置项以及可直接运行的代码示例（示例取自各模块自身的 test-cases）。

English: [User Guide index](../README.md)

## 选择版本线

| 目录 | 分支 | 版本线 | 适用场景 |
|---|---|---|---|
| [`current/`](../current/README.md) | `release` | 0.2.x（最新发布：**0.2.16**） | Java 17+，Spring Boot 3.x / Spring Cloud 2022.0.x – 2025.0.x |
| [`1.x/`](../1.x/README.md) | `release-1.x` | 0.1.x（最新发布：**0.1.16**） | Java 8+，Spring Boot 2.x / Spring Cloud Hoxton – 2021.0.x |

## 版本兼容矩阵

| microsphere-mybatis | Java | MyBatis | MyBatis-Spring | mybatis-spring-boot-starter | Spring Cloud |
|---|---|---|---|---|---|
| **0.2.x**（`current`） | 17、21、25（CI 验证；基线 17） | 3.5.19 | 4.1.0 | 3.0.5 | 2022.0.x – 2025.0.x |
| **0.1.x**（`1.x`） | 8、11、17、21、25（CI 验证；基线 8） | 3.5.19 | 2.1.2 | 2.1.2 | Hoxton – 2021.0.x |

上游微球 BOM 同样按版本线锁定：`current` 使用 `microsphere-spring-cloud` **0.2.26**，
`1.x` 使用 **0.1.26**。

## 模块一览

| 模块 | 用途 |
|---|---|
| [microsphere-mybatis-utils](../current/zh/utils.md) | 不依赖 Spring 即可引导 MyBatis 的静态工具类 `MyBatisUtils` |
| [microsphere-mybatis-core](../current/zh/core.md) | Executor 级 `ExecutorFilter` / `ExecutorInterceptor` 链与 MyBatis `Interceptor` 桥接 |
| [microsphere-mybatis-test](../current/zh/test.md) | JUnit 5 扩展 `@MyBatisTest`、抽象测试基类、固件实体/Mapper 与 H2 脚本 |
| [microsphere-mybatis-spring](../current/zh/spring.md) | 原生 Spring 注解驱动：`@EnableMyBatis`、`@MyBatisConfiguration`、`@EnableMyBatisExtension` |
| [microsphere-mybatis-spring-test](../current/zh/spring-test.md) | 复用固件数据源与数据库生命周期的 Spring 测试配置 |
| [microsphere-mybatis-spring-boot](../current/zh/spring-boot.md) | Executor 扩展的 Spring Boot 自动配置 |
| [microsphere-mybatis-spring-cloud](../current/zh/spring-cloud.md) | Spring Cloud 特性目录（Actuator `HasFeatures`） |
| [parent 与 dependencies](../current/zh/parent-and-dependencies.md) | 用于版本管理的父 POM 与已发布的 BOM |

## 获取构件

所有模块均以 `io.github.microsphere-projects` 发布至 Maven Central。
导入 BOM 后只需声明所需模块（版本由 BOM 管理）：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-mybatis-dependencies</artifactId>
            <version>0.2.16</version> <!-- 1.x 线使用 0.1.16 -->
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-mybatis-spring-boot</artifactId>
    </dependency>
</dependencies>
```

> **注意：** BOM 未管理 `microsphere-mybatis-utils`；若直接依赖它需显式声明版本。
> 依赖 `microsphere-mybatis-core` 时会自动传递引入。

微球各模块对上游依赖（MyBatis、Spring、JUnit、H2 等）均声明为 `optional`，
由应用方通过自己的 BOM 控制实际版本。
