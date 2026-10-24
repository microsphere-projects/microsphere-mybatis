# Microsphere MyBatis Documentation

Welcome to the documentation of **Microsphere MyBatis**, a library that extends MyBatis with an
Executor-level filter/interceptor chain and provides first-class Spring, Spring Boot, Spring Cloud
and JUnit 5 integrations.

- 中文版本请见 [文档首页（中文）](zh/README.md)

## Guides

| Guide | Description | Chinese |
|---|---|---|
| [User Guide](user-guide/README.md) | Installation, APIs, configuration properties and code examples for every Maven module | [用户文档](user-guide/zh/README.md) |
| [Developer Guide](developer-guide/README.md) | Architecture design, source-code analysis, design patterns and flow diagrams | [开发者文档](developer-guide/zh/README.md) |

## Source Diffs

Tag-to-tag source comparisons, one pair per comparison and one file per source set. Java
`8` ↔ `17` syntax/API differences and Java EE ↔ Jakarta EE differences are filtered out, so the tables
show only genuine implementation divergence (excluded items are still listed for traceability).

| Comparison | `src/main` | `src/test` | Scope |
|---|---|---|---|
| `0.2.16` vs `0.1.16` | [main-diff-0.2.16-vs-0.1.16.md](diff/source/main-diff-0.2.16-vs-0.1.16.md) · [中文](diff/source/main-diff-0.2.16-vs-0.1.16.zh.md) | [test-diff-0.2.16-vs-0.1.16.md](diff/source/test-diff-0.2.16-vs-0.1.16.md) · [中文](diff/source/test-diff-0.2.16-vs-0.1.16.zh.md) | Newest tag of each release line; all 9 Maven modules match by name |

Headline result: after filtering, `src/main` retains 3 differences (one JDK `Map` API substitution in
`Plugins.java` plus the two legacy `spring.factories` registrations carried only by 0.1.x) and
`src/test` retains none.

## Documented Branches and Versions

Documentation is organized per released branch line. Each guide contains one directory per branch:

| Directory | Git Branch | Version Line | Latest Release | Target Platform |
|---|---|---|---|---|
| `current` | [`release`](https://github.com/microsphere-projects/microsphere-mybatis/tree/release) | 0.2.x | 0.2.16 | Java 17+ · Spring Boot 3.x · Spring Cloud 2022.0.x – 2025.0.x |
| `1.x` | [`release-1.x`](https://github.com/microsphere-projects/microsphere-mybatis/tree/release-1.x) | 0.1.x | 0.1.16 | Java 8+ · Spring Boot 2.x · Spring Cloud Hoxton – 2021.0.x |

The source code of the two branches is functionally equivalent. The differences are:

1. **Dependency generation** — `current` compiles against MyBatis-Spring `4.1.0` and
   mybatis-spring-boot-starter `3.0.5`; `1.x` compiles against MyBatis-Spring `2.1.2` and
   mybatis-spring-boot-starter `2.1.2` (both use MyBatis `3.5.19`).
2. **Auto-configuration registration** — both lines register through
   `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`; `1.x`
   additionally ships the legacy `META-INF/spring.factories` so Spring Boot consumers older than 2.7
   (which cannot read the `.imports` file) still discover the auto-configurations. See
   [main-diff-0.2.16-vs-0.1.16.md](diff/source/main-diff-0.2.16-vs-0.1.16.md).
3. **Java baseline** — `current` requires Java 17 (it uses pattern matching for `instanceof`);
   `1.x` is built for Java 8 and is CI-tested on Java 8/11/17/21/25.

## Directory Layout

```
docs/
├── README.md                     # this file
├── zh/README.md                  # 中文文档首页
├── user-guide/
│   ├── README.md                 # User Guide index / version compatibility
│   ├── zh/README.md              # 用户文档索引
│   ├── current/                  # 0.2.x modules (English)
│   │   ├── utils.md
│   │   ├── core.md
│   │   ├── test.md
│   │   ├── spring.md
│   │   ├── spring-test.md
│   │   ├── spring-boot.md
│   │   ├── spring-cloud.md
│   │   ├── parent-and-dependencies.md
│   │   └── zh/                   # 中文（同名文件）
│   └── 1.x/                      # 0.1.x modules (English), same layout + zh/
├── developer-guide/
│   ├── README.md                 # Developer Guide index
│   ├── zh/README.md              # 开发者文档索引
│   ├── current/                  # 0.2.x architecture & module analysis
│   │   ├── architecture.md       # overall architecture + module dependency graph
│   │   ├── <module>.md × 7
│   │   └── zh/
│   └── 1.x/                      # 0.1.x, same layout + zh/
└── diff/
    └── source/                   # tag-to-tag source comparisons
        ├── main-diff-0.2.16-vs-0.1.16.md    # src/main comparison (English)
        ├── main-diff-0.2.16-vs-0.1.16.zh.md # src/main 对比（中文）
        ├── test-diff-0.2.16-vs-0.1.16.md    # src/test comparison (English)
        └── test-diff-0.2.16-vs-0.1.16.zh.md # src/test 对比（中文）
```

## License

All documentation is released under the [Apache License, Version 2.0](../LICENSE).
