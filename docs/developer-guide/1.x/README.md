# microsphere-mybatis 1.x — Developer Guide (`release-1.x`)

[中文](zh/README.md) · [← Developer Guide Index](../README.md) · [User Guide (1.x)](../../user-guide/1.x/README.md)

Version line: **0.1.x** (`revision` = `0.1.17-SNAPSHOT` on the branch tip), Java 8 baseline,
Spring 5 / Spring Boot 2 / Spring Cloud Hoxton–2021 generation.

The implementation is a near-perfect mirror of the [`current` line](../current/README.md).
A whole-branch diff shows only **six source files** differ, and none change behaviour.
These documents therefore record the differences and link back to `current/` for the full
architecture and source analysis.

## Verified source diffs (`release` vs `release-1.x`)

| File | Difference | Why |
|---|---|---|
| `core/…/InterceptingExecutorInterceptor.java` | classic `instanceof` + cast instead of pattern-matching `instanceof` | Java 8 target (pattern matching needs 16+) |
| `core/…/Plugins.java` | `org.apache.ibatis.util.MapUtil.computeIfAbsent(map, k, fn)` instead of `Map#computeIfAbsent` | `Map` default methods exist in 8, but MyBatis' own util was used at authoring time; equivalent semantics |
| `test/…/MyBatisTestExtension.java` | classic `instanceof AutoCloseable` + cast | Java 8 target |
| `test/…/MyBatisTestExtensionTest.java` | extra `@MyBatisRuntime private static …` fields (Configuration, Environment, SqlSessionFactory, DataSource, Properties, SqlSession, Transaction) | test-fixture drift only |
| `spring-boot` resources | **adds** `META-INF/spring.factories` (`EnableAutoConfiguration=…`) while **keeping** `AutoConfiguration.imports` | Boot 2.x (<2.7) only reads `spring.factories` |
| `spring-cloud` resources | same dual registration | same |

POM diffs: `pom.xml` (parent `microsphere-spring-cloud-parent` 0.1.26, no `java.version`
property override → inherited Java 8 target) and `microsphere-mybatis-parent/pom.xml`
(mybatis 3.5.19, mybatis-spring 2.1.2, mybatis-spring-boot-starter 2.1.2).

## Module documents

| Module | Doc | Delta vs current |
|---|---|---|
| utils | [utils.md](utils.md) | none |
| core | [core.md](core.md) | 2 files, Java-8 syntax only |
| test | [test.md](test.md) | 1 main file + 1 test file, syntax only |
| spring | [spring.md](spring.md) | none |
| spring-test | [spring-test.md](spring-test.md) | none |
| spring-boot | [spring-boot.md](spring-boot.md) | dual auto-config registration |
| spring-cloud | [spring-cloud.md](spring-cloud.md) | dual auto-config registration |
| parent / BOM | [parent-and-dependencies.md](parent-and-dependencies.md) | 0.1.x generation versions |

Also: [architecture.md](architecture.md) — same layering as current; only the external
dependency generation changes.

## CI matrix (1.x)

`maven-build.yml`: `java: [8, 11, 17, 21, 25]` × `spring-cloud-hoxton / 2020 / 2021`
(15 combinations) — versus current's `[17, 21, 25]` × `2022–2025`.
