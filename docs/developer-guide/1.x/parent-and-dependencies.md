# Parent & Dependencies POMs (1.x) — Developer Guide

[中文](zh/parent-and-dependencies.md) · [← 1.x Index](README.md) · [Current analysis](../current/parent-and-dependencies.md)

Same three-POM structure (root aggregator → `microsphere-mybatis-parent` →
`microsphere-mybatis-dependencies` BOM), same `${revision}` + flatten scheme, same
optional-dependency policy, same six-artifact BOM scope (utils still absent). Only the
version generation and CI matrix change. See
[current/parent-and-dependencies.md](../current/parent-and-dependencies.md) for the
inheritance diagram and rationale.

## Version table (`release-1.x`)

| Item | 1.x | current |
|---|---|---|
| Root parent | `microsphere-spring-cloud-parent` **0.1.26** | 0.2.26 |
| `revision` | `0.1.17-SNAPSHOT` | `0.2.17-SNAPSHOT` |
| `mybatis.version` | 3.5.19 | 3.5.19 |
| `mybatis-spring.version` | **2.1.2** (javax era, Spring 5) | 4.1.0 (jakarta era, Spring 6) |
| `mybatis-spring-boot.version` (starter) | **2.1.2** | 3.0.5 |
| `cglib.version` | 3.3.0 | 3.3.0 |
| `microsphere-spring-cloud.version` (BOM import) | 0.1.26 | 0.2.26 |
| Java baseline | 8 — the 1.x root POM has **no** `java.version` property, the target comes from the upstream 0.1.26 parent | 17 (explicit `java.version` property) |

## CI matrix diff

| | 1.x | current |
|---|---|---|
| `java` | 8, 11, 17, 21, 25 | 17, 21, 25 |
| `maven-profile-spring-cloud` | hoxton, 2020, 2021 | 2022, 2023, 2024, 2025 |
| combinations | 15 | 12 |

Both workflows run `mvn --activate-profiles test,coverage,<profile> -Drevision=0.0.1-SNAPSHOT`,
so the published compatibility statement — 0.1.x supports Spring Cloud Hoxton – 2021.0.x on
Java 8+ — is the matrix, not prose.

## See Also

- [1.x differences overview](README.md)
- [User Guide](../../user-guide/1.x/parent-and-dependencies.md)
