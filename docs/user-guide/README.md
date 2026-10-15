# User Guide

The User Guide explains how to install and use every Maven module of Microsphere MyBatis:
Maven coordinates, API references, configuration properties and runnable code examples
(examples are taken from the modules' own test-cases).

中文：[用户文档索引](zh/README.md)

## Pick Your Version Line

| Directory | Branch | Version Line | Use It When |
|---|---|---|---|
| [`current/`](current/README.md) | `release` | 0.2.x (latest release: **0.2.16**) | You run Java 17+ with Spring Boot 3.x / Spring Cloud 2022.0.x – 2025.0.x |
| [`1.x/`](1.x/README.md) | `release-1.x` | 0.1.x (latest release: **0.1.16**) | You run Java 8+ with Spring Boot 2.x / Spring Cloud Hoxton – 2021.0.x |

## Compatibility Matrix

| microsphere-mybatis | Java | MyBatis | MyBatis-Spring | mybatis-spring-boot-starter | Spring Cloud |
|---|---|---|---|---|---|
| **0.2.x** (`current`) | 17, 21, 25 (CI-tested; baseline 17) | 3.5.19 | 4.1.0 | 3.0.5 | 2022.0.x – 2025.0.x |
| **0.1.x** (`1.x`) | 8, 11, 17, 21, 25 (CI-tested; baseline 8) | 3.5.19 | 2.1.2 | 2.1.2 | Hoxton – 2021.0.x |

The upstream microsphere bill of materials is pinned per line as well:
`current` uses `microsphere-spring-cloud` **0.2.26**, `1.x` uses **0.1.26**.

## Modules at a Glance

| Module | Purpose |
|---|---|
| [microsphere-mybatis-utils](current/utils.md) | Static utilities to bootstrap MyBatis without Spring (`MyBatisUtils`) |
| [microsphere-mybatis-core](current/core.md) | Executor-level `ExecutorFilter` / `ExecutorInterceptor` chain, MyBatis `Interceptor` bridge |
| [microsphere-mybatis-test](current/test.md) | JUnit 5 extension `@MyBatisTest`, abstract test bases, fixture entities/mappers, H2 scripts |
| [microsphere-mybatis-spring](current/spring.md) | Annotation-driven MyBatis on plain Spring: `@EnableMyBatis`, `@MyBatisConfiguration`, `@EnableMyBatisExtension` |
| [microsphere-mybatis-spring-test](current/spring-test.md) | Spring test configurations reusing the fixture datasource and database lifecycle |
| [microsphere-mybatis-spring-boot](current/spring-boot.md) | Spring Boot auto-configuration for the Executor extension |
| [microsphere-mybatis-spring-cloud](current/spring-cloud.md) | Spring Cloud feature catalogue (Actuator `HasFeatures`) |
| [parent & dependencies](current/parent-and-dependencies.md) | Parent POM and published BOM used for version management |

## Getting the Artifacts

All modules are published to Maven Central under `io.github.microsphere-projects`.
Import the BOM and declare only the modules you need (versions are managed for you):

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-mybatis-dependencies</artifactId>
            <version>0.2.16</version> <!-- 0.1.16 for the 1.x line -->
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

> **Note:** `microsphere-mybatis-utils` is not managed by the BOM; declare its version explicitly
> if you depend on it directly. It is pulled transitively by `microsphere-mybatis-core`.

Most upstream dependencies (MyBatis, Spring, JUnit, H2 …) are declared `optional` by the
microsphere modules, so the consuming application controls the versions through its own BOM.
