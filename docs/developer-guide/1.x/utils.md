# microsphere-mybatis-utils (1.x) — Developer Guide

[中文](zh/utils.md) · [← 1.x Index](README.md) · [Current analysis](../current/utils.md)

**No source differences vs `current`.** The `MyBatisUtils` bootstrap API
(`loadProperties` / `getConfiguration` / `buildSqlSessionFactory` / `openSession` /
`newExecutor` / `runScript` / `doIn*` templates) is byte-for-byte the same design; see
[current/utils.md](../current/utils.md) for the full source analysis and flow diagrams.

1.x-line facts that matter when reading that analysis:

| Aspect | 1.x value |
|---|---|
| Java baseline | 8 — the code already avoided post-8 syntax, hence zero diffs |
| MyBatis version compiled against | 3.5.19 (same as current) |
| ScriptRunner behaviour | identical (auto-commit false, stop-on-error false) |

## See Also

- [1.x differences overview](README.md)
- [User guide](../../user-guide/1.x/utils.md)
