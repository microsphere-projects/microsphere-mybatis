# 用户文档 —— `current`（0.2.x，`release` 分支）

文档对应最新发布版本：**0.2.16** · Java 17+ · Spring Boot 3.x · Spring Cloud 2022.0.x – 2025.0.x
· MyBatis 3.5.19 · MyBatis-Spring 4.1.0 · mybatis-spring-boot-starter 3.0.5

English：[本目录索引](../README.md) · 上一代版本线：[1.x](../../1.x/zh/README.md)

## 模块文档

1. [microsphere-mybatis-utils](utils.md) —— 无需容器的 MyBatis 引导工具
2. [microsphere-mybatis-core](core.md) —— Executor 过滤器/拦截器链（**项目核心**）
3. [microsphere-mybatis-test](test.md) —— JUnit 5 `@MyBatisTest` 扩展与抽象测试基类
4. [microsphere-mybatis-spring](spring.md) —— `@EnableMyBatis` / `@MyBatisConfiguration` / `@EnableMyBatisExtension`
5. [microsphere-mybatis-spring-test](spring-test.md) —— 测试固件 Spring 配置
6. [microsphere-mybatis-spring-boot](spring-boot.md) —— 自动配置（`AutoConfiguration.imports`）
7. [microsphere-mybatis-spring-cloud](spring-cloud.md) —— `HasFeatures` 特性目录
8. [parent 与 dependencies](parent-and-dependencies.md) —— 父 POM 与 BOM

## 快速开始

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-mybatis-dependencies</artifactId>
            <version>0.2.16</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

按技术栈选择模块：Spring Boot 应用用 `microsphere-mybatis-spring-boot`；原生 Spring 用
`microsphere-mybatis-spring`；不使用 Spring 时直接用 `microsphere-mybatis-core`
（及 `microsphere-mybatis-utils`）。
