# User Guide — `1.x` (0.1.x, `release-1.x` branch)

Latest release documented: **0.1.16** · Java 8+ · Spring Boot 2.x · Spring Cloud Hoxton – 2021.0.x
· MyBatis 3.5.19 · MyBatis-Spring 2.1.2 · mybatis-spring-boot-starter 2.1.2

English index: [1.x README](README.md) · 中文：[1.x 文档索引](zh/README.md) · 现代版本线：[current](../current/README.md)

## How the 1.x line differs from `current` (0.2.x)

| Aspect | `1.x` (0.1.x) | `current` (0.2.x) |
|---|---|---|
| Java baseline | 8 (CI-tested 8/11/17/21/25) | 17 (CI-tested 17/21/25) |
| MyBatis-Spring | 2.1.2 | 4.1.0 |
| mybatis-spring-boot-starter | 2.1.2 | 3.0.5 |
| Spring Cloud | Hoxton – 2021.0.x | 2022.0.x – 2025.0.x |
| Upstream BOM | microsphere-spring-cloud 0.1.26 | 0.2.26 |
| Boot auto-config registration | `META-INF/spring.factories` (`EnableAutoConfiguration=` entry) | `META-INF/spring/...AutoConfiguration.imports` |
| `MyBatisAutoConfiguration` annotations | `@ConditionalOnMyBatisAvailable` + `@EnableMyBatisExtension` + `@AutoConfigureAfter` | same |
| Source differences | classic `instanceof` casts, `MapUtil.computeIfAbsent` in `Plugins` | pattern matching, `java.util.Map#computeIfAbsent` |

The **public Java API is identical** between the two lines — every class, method and annotation
attribute documented for `current` exists in 0.1.x with the same signature (all types are
`@since 1.0.0`). For that reason the pages in this directory give the 1.x-specific facts
(coordinates, versions, registration files) and link to the `current` pages for the full API
reference; nothing on those pages changed except where noted.

## Modules

1. [microsphere-mybatis-utils](utils.md)
2. [microsphere-mybatis-core](core.md)
3. [microsphere-mybatis-test](test.md)
4. [microsphere-mybatis-spring](spring.md)
5. [microsphere-mybatis-spring-test](spring-test.md)
6. [microsphere-mybatis-spring-boot](spring-boot.md)
7. [microsphere-mybatis-spring-cloud](spring-cloud.md)
8. [parent & dependencies](parent-and-dependencies.md)

## Quick Start

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-mybatis-dependencies</artifactId>
            <version>0.1.16</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

> **Do not mix lines.** A Spring Boot 2.x / javax application must use 0.1.x artifacts;
> importing 0.2.x there fails at runtime (`jakarta` classes missing, MyBatis-Spring 4 API drift).
