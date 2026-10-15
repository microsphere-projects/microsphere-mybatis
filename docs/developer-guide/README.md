# Developer Guide

The Developer Guide explains how Microsphere MyBatis is built: architecture design, source-code
analysis of every Maven module, the design patterns in use, and flow diagrams (Mermaid) for the
non-obvious execution paths.

中文：[开发者文档索引](zh/README.md)

## Pick Your Version Line

| Directory | Branch | Version Line |
|---|---|---|
| [`current/`](current/architecture.md) | `release` | 0.2.x (Java 17, MyBatis-Spring 4.1.0, `AutoConfiguration.imports`) |
| [`1.x/`](1.x/architecture.md) | `release-1.x` | 0.1.x (Java 8, MyBatis-Spring 2.1.2, `spring.factories`) |

The implementation of the two lines is nearly identical; `1.x/` documents focus on what differs.

## Where to Start

1. [Overall architecture & module dependency graph (current)](current/architecture.md) — layering,
   dependency flow, external dependency strategy, and the end-to-end request interception overview.
2. [Overall architecture & module dependency graph (1.x)](1.x/architecture.md).

## Module Deep-Dives

| Module | Documentation (current) | Documentation (1.x) | Core Topics |
|---|---|---|---|
| `microsphere-mybatis-utils` | [utils.md](current/utils.md) | [utils.md](1.x/utils.md) | Configuration bootstrap, resource-driven `ScriptRunner` |
| `microsphere-mybatis-core` | [core.md](current/core.md) | [core.md](1.x/core.md) | Filter chain, interceptor adapter, plugin bridging, `CachingExecutor` unwrapping |
| `microsphere-mybatis-test` | [test.md](current/test.md) | [test.md](1.x/test.md) | JUnit 5 extension lifecycle, resolver SPI via `ServiceLoader` |
| `microsphere-mybatis-spring` | [spring.md](current/spring.md) | [spring.md](1.x/spring.md) | `ImportBeanDefinitionRegistrar` family, annotation attribute → BeanProperty mapping |
| `microsphere-mybatis-spring-test` | [spring-test.md](current/spring-test.md) | [spring-test.md](1.x/spring-test.md) | Event-driven database lifecycle |
| `microsphere-mybatis-spring-boot` | [spring-boot.md](current/spring-boot.md) | [spring-boot.md](1.x/spring-boot.md) | Auto-configuration ordering and conditions |
| `microsphere-mybatis-spring-cloud` | [spring-cloud.md](current/spring-cloud.md) | [spring-cloud.md](1.x/spring-cloud.md) | Feature catalogue binding (`features.yaml`) |
| `parent` / `dependencies` | [parent-and-dependencies.md](current/parent-and-dependencies.md) | [parent-and-dependencies.md](1.x/parent-and-dependencies.md) | POM inheritance vs. BOM import, `${revision}` flatten |

## Design Patterns Used (Index)

| Pattern | Where |
|---|---|
| Chain of Responsibility | `ExecutorFilterChain.process(...)` advancing `position` over `filters[]` |
| Decorator | `InterceptingExecutor` wrapping the real `Executor` |
| Adapter | `InterceptorsExecutorFilterAdapter` turning `ExecutorInterceptor[]` into one `ExecutorFilter` |
| Observer / Callback | `beforeXxx` / `afterXxx` pairs of `ExecutorInterceptor` |
| Template Method | `AbstractMyBatisTest` hierarchy, `AbstractComponentResolver.doResolve(...)` |
| Strategy | `ComponentResolver` implementations selected by `supportsParameter`/`supportsField` |
| Service Provider (SPI) | `ServiceLoader` registration of `ComponentResolver` |
| Static Factory / Utility | `MyBatisUtils`, `Executors`, `Plugins` (abstract classes with private constructors) |
| Front Controller-ish registrar family | `MyBatisImportBeanDefinitionRegistrar` base + three concrete registrars |
| Bean Post-Processor | `SqlSessionFactoryBeanPostProcessor` injecting the plugin |

## Build & Test

- Build: `./mvnw build` (or `mvnw.cmd build` on Windows).
- CI matrix (current line): Java 17 / 21 / 25 × Spring Cloud profiles `2022`–`2025`;
  the 1.x line additionally verifies Java 8 and 11 with Spring Cloud `hoxton`–`2021`.
- Tests rely on in-memory H2 (`jdbc:h2:mem:test_mem`) — no external database is required.
