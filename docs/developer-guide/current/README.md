# Developer Guide — `current` (release / 0.2.x)

[中文](zh/README.md) · [← Developer Guide Index](../README.md) · [User Guide (current)](../../user-guide/current/README.md)

Source analysis of the `release` branch: Java 17 baseline, MyBatis 3.5.19, MyBatis-Spring 4.1.0,
Spring Boot 3-style `AutoConfiguration.imports`.

## Reading Order

1. [architecture.md](architecture.md) — layering, module dependency graph, external
   dependency strategy, and the end-to-end interception sequence.
2. Then the module deep-dives, in dependency order:

| # | Document | Module | Core Topics |
|---|---|---|---|
| 1 | [utils.md](utils.md) | `microsphere-mybatis-utils` | `MyBatisUtils` bootstrap, `ScriptRunner`, `doIn*` templates |
| 2 | [core.md](core.md) | `microsphere-mybatis-core` | filter chain, `InterceptingExecutor`, adapter, `CachingExecutor` unwrapping |
| 3 | [test.md](test.md) | `microsphere-mybatis-test` | JUnit 5 extension lifecycle, `ComponentResolver` SPI |
| 4 | [spring.md](spring.md) | `microsphere-mybatis-spring` | registrar family, annotation → bean-property mapping |
| 5 | [spring-test.md](spring-test.md) | `microsphere-mybatis-spring-test` | event-driven database lifecycle |
| 6 | [spring-boot.md](spring-boot.md) | `microsphere-mybatis-spring-boot` | auto-configuration conditions & ordering |
| 7 | [spring-cloud.md](spring-cloud.md) | `microsphere-mybatis-spring-cloud` | `features.yaml` → `HasFeatures` binding |
| 8 | [parent-and-dependencies.md](parent-and-dependencies.md) | parent / BOM | POM inheritance, `${revision}` flatten, CI matrix |

## Conventions in these documents

- Each deep-dive covers: **Architecture Design** (with Mermaid diagrams for non-obvious
  flows), **Source Analysis** (with `file:line` references into the `release` tree),
  **Design Patterns**, **Known Limitations**, and cross-links.
- Claims reference the branch they were verified against; where the 1.x line differs, a
  link to `../1.x/<module>.md` is provided.
