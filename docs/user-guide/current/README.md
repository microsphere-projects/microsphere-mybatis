# User Guide — `current` (0.2.x, `release` branch)

Latest release documented: **0.2.16** · Java 17+ · Spring Boot 3.x · Spring Cloud 2022.0.x – 2025.0.x
· MyBatis 3.5.19 · MyBatis-Spring 4.1.0 · mybatis-spring-boot-starter 3.0.5

中文：[本目录文档索引](zh/README.md) · 上一代版本线：[1.x](../1.x/README.md)

## Modules

1. [microsphere-mybatis-utils](utils.md) — container-free MyBatis bootstrapping utilities
2. [microsphere-mybatis-core](core.md) — Executor filter/interceptor chain (**the heart of the project**)
3. [microsphere-mybatis-test](test.md) — JUnit 5 `@MyBatisTest` extension & abstract test bases
4. [microsphere-mybatis-spring](spring.md) — `@EnableMyBatis` / `@MyBatisConfiguration` / `@EnableMyBatisExtension`
5. [microsphere-mybatis-spring-test](spring-test.md) — fixture Spring configurations for tests
6. [microsphere-mybatis-spring-boot](spring-boot.md) — auto-configuration (`AutoConfiguration.imports`)
7. [microsphere-mybatis-spring-cloud](spring-cloud.md) — `HasFeatures` catalogue
8. [parent & dependencies](parent-and-dependencies.md) — parent POM and BOM

## Quick Start

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

Then pick the module for your stack: Spring Boot apps → `microsphere-mybatis-spring-boot`;
plain Spring → `microsphere-mybatis-spring`; no Spring at all → `microsphere-mybatis-core`
(+ `microsphere-mybatis-utils`).
