# Microsphere MyBatis

> Microsphere Projects for MyBatis

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/microsphere-projects/microsphere-mybatis)
[![Maven Build](https://github.com/microsphere-projects/microsphere-mybatis/actions/workflows/maven-build.yml/badge.svg)](https://github.com/microsphere-projects/microsphere-mybatis/actions/workflows/maven-build.yml)
[![Codecov](https://codecov.io/gh/microsphere-projects/microsphere-mybatis/branch/main/graph/badge.svg)](https://app.codecov.io/gh/microsphere-projects/microsphere-mybatis)
![Maven](https://img.shields.io/maven-central/v/io.github.microsphere-projects/microsphere-mybatis-dependencies.svg)
![License](https://img.shields.io/github/license/microsphere-projects/microsphere-mybatis.svg)

Microsphere MyBatis is a lightweight extension framework for MyBatis that provides powerful SQL statement interception
capabilities. It enables developers to monitor, modify, or enhance SQL execution without changing your existing MyBatis
code base.

## Purpose and Scope

Microsphere MyBatis is provides an extensible framework for enhancing MyBatis functionality through a sophisticated
interception pipeline. The framework enables developers to inject custom processing logic into MyBatis database
operations without modifying existing application code, following the principle of separation of concerns:

- Monitor SQL execution for logging, metrics, and observability
- Apply cross-cutting concerns to SQL operations (security, throttling, caching)
- Enhance SQL execution with additional capabilities
- Integrate with other Microsphere projects like Sentinel, Resilience4j, and Observability

## Modules

| **Module**                           | **Purpose**                                       |
|--------------------------------------|---------------------------------------------------|
| **microsphere-mybatis-core**         | Core MyBatis framework                            |
| **microsphere-mybatis-test**         | Base testing utilities and infrastructure         |
| **microsphere-mybatis-spring**       | Spring Framework integration and configuration    |
| **microsphere-mybatis-spring-test**  | Spring Testing integration                        |
| **microsphere-mybatis-spring-boot**  | Spring Boot auto-configuration and properties     |
| **microsphere-mybatis-spring-cloud** | Spring Cloud features integration                 |
| **microsphere-mybatis-parent**       | Spring-specific testing utilities                 |
| **microsphere-mybatis-dependencies** | Bill of Materials (BOM) for dependency management |

## Getting Started

The easiest way to get started is by adding the Microsphere MyBatis BOM (Bill of Materials) to your project's
pom.xml:

```xml

<dependencyManagement>
    <dependencies>
        ...
        <!-- Microsphere MyBatis Dependencies -->
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-mybatis-dependencies</artifactId>
            <version>${microsphere-mybatis.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        ...
    </dependencies>
</dependencyManagement>
```

`${microsphere-mybatis.version}` has two branches:

| **Branches** | **Purpose**                                      | **Latest Version** |
|--------------|--------------------------------------------------|--------------------|
| **main**     | Compatible with Spring Cloud 2022.0.x - 2025.0.x | `0.2.7`            |
| **1.x**      | Compatible with Spring Cloud Hoxton - 2021.0.x   | `0.1.7`            |

Then add the specific modules you need.

### Maven Dependencies

```xml

<dependencies>
    <!-- Microsphere MyBatis Core -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-mybatis-core</artifactId>
    </dependency>
</dependencies>
```

### Gradle Dependencies

```kotlin
implementation(platform("io.github.microsphere-projects:microsphere-mybatis-core:${microsphere.mybatis.version}"))
```

## Building from Source

You don't need to build from source unless you want to try out the latest code or contribute to the project.

To build the project, follow these steps:

1. Clone the repository:

```bash
git clone https://github.com/microsphere-projects/microsphere-mybatis.git
```

2. Build the source:

- Linux/MacOS:

```bash
./mvnw build
```

- Windows:

```powershell
mvnw.cmd build
```

## Contributing

We welcome your contributions! Please read [Code of Conduct](./CODE_OF_CONDUCT.md) before submitting a pull request.

## Reporting Issues

* Before you log a bug, please search
  the [issues](https://github.com/microsphere-projects/microsphere-mybatis/issues)
  to see if someone has already reported the problem.
* If the issue doesn't already
  exist, [create a new issue](https://github.com/microsphere-projects/microsphere-mybatis/issues/new).
* Please provide as much information as possible with the issue report.

## Documentation

### User Guide

[DeepWiki Host](https://deepwiki.com/microsphere-projects/microsphere-mybatis)

[ZRead Host](https://zread.ai/microsphere-projects/microsphere-mybatis)

### Wiki

[Github Host](https://github.com/microsphere-projects/microsphere-mybatis/wiki)

### JavaDoc

- [microsphere-mybatis-core](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-mybatis-core)
- [microsphere-mybatis-test](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-mybatis-test)
- [microsphere-mybatis-spring](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-mybatis-spring)
- [microsphere-mybatis-spring-test](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-mybatis-spring-test)
- [microsphere-mybatis-spring-boot](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-mybatis-spring-boot)
- [microsphere-mybatis-spring-cloud](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-mybatis-spring-cloud)

## License

Microsphere MyBatis is licensed under
the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
