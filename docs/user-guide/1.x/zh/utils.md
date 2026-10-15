# microsphere-mybatis-utils —— 用户文档（1.x / 0.1.x）

[English](../utils.md) · [开发者文档](../../../developer-guide/1.x/zh/utils.md) · [← 1.x 索引](README.md) · [完整 API 参考（current）](../../current/zh/utils.md)

`MyBatisUtils` 静态工具 API 与 0.2.x 线完全一致，完整方法说明与示例见
[current/zh/utils.md](../../current/zh/utils.md)。本页仅记录 1.x 特有信息。

## 版本信息

| 项目 | 值 |
|---|---|
| 构件 | `io.github.microsphere-projects:microsphere-mybatis-utils` |
| 文档对应版本 | `0.1.16`（`release-1.x` 分支，0.1.x 线） |
| Java 基线 | **8** —— 未使用 Java 8 之外的语言特性 |
| 依赖 | MyBatis `3.5.19`（optional） |
| API | 与 `current` 相同（均 `@since 1.0.0`） |

## 安装

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-mybatis-utils</artifactId>
    <version>0.1.16</version>
</dependency>
```

## 1.x 说明

- 0.1.x 的 BOM 同样**不管理**本构件版本。
- API 面：`loadProperties`、`getConfiguration`（3 个重载）、`getEnvironment`、`getDataSource`、
  `buildSqlSessionFactory`、`openSession`、`getConnection`、`newTransaction`、`newExecutor`、
  `runScript`（2 个重载）、`doInSqlSession` / `doInConnection` / `doInExecutor`，语义与
  [current 参考页](../../current/zh/utils.md)一致。
