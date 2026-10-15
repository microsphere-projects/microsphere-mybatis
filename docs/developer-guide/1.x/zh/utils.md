# microsphere-mybatis-utils（1.x）—— 开发者文档

[English](../utils.md) · [← 1.x 索引](README.md) · [current 源码分析](../../current/zh/utils.md)

**与 `current` 无源码差异。** `MyBatisUtils` 引导 API（`loadProperties` / `getConfiguration` /
`buildSqlSessionFactory` / `openSession` / `newExecutor` / `runScript` / `doIn*` 模板）逐字节
同设计；完整源码分析与流程图见
[current/utils.md](../../current/zh/utils.md)。

阅读该分析时需要注意的 1.x 线事实：

| 方面 | 1.x 取值 |
|---|---|
| Java 基线 | 8 —— 该代码本就未使用 8 之后的语法，因此零差异 |
| 编译所用 MyBatis 版本 | 3.5.19（与 current 相同） |
| ScriptRunner 行为 | 相同（auto-commit 为 false，出错不停止） |

## 另见

- [1.x 差异总览](README.md)
- [用户文档](../../../user-guide/1.x/zh/utils.md)
