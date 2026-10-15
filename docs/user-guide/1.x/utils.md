# microsphere-mybatis-utils — User Guide (1.x / 0.1.x)

[中文](zh/utils.md) · [Developer Guide](../../developer-guide/1.x/utils.md) · [← 1.x Index](README.md) · [完整 API 参考（current）](../current/utils.md)

Same `MyBatisUtils` static utility API as the 0.2.x line — see
[current/utils.md](../current/utils.md) for the full method reference and examples.
This page records the 1.x specifics.

## Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-mybatis-utils` |
| Documented version | `0.1.16` (branch `release-1.x`, 0.1.x line) |
| Java baseline | **8** — compiled without language features beyond Java 8 |
| Requires | MyBatis `3.5.19` (declared `optional`) |
| API | Identical to `current` (all types `@since 1.0.0`) |

## Installation

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-mybatis-utils</artifactId>
    <version>0.1.16</version>
</dependency>
```

## 1.x Notes

- The BOM (`microsphere-mybatis-dependencies:0.1.16`) does not manage this artifact either.
- API surface in 0.1.16: `loadProperties`, `getConfiguration` (3 overloads),
  `getEnvironment`, `getDataSource`, `buildSqlSessionFactory`, `openSession`, `getConnection`,
  `newTransaction`, `newExecutor`, `runScript` (2 overloads),
  `doInSqlSession` / `doInConnection` / `doInExecutor` — identical semantics to
  [the current reference](../current/utils.md).
