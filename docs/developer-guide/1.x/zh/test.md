# microsphere-mybatis-test（1.x）—— 开发者文档

[English](../test.md) · [← 1.x 索引](README.md) · [current 源码分析](../../current/zh/test.md)

JUnit 5 扩展、Resolver SPI、抽象层级与测试夹具均为同一设计 —— 生命周期图与 store-key
缓存微妙点见 [current/test.md](../../current/zh/test.md)。本分支两个文件不同。

## 源码差异

### 1. `MyBatisTestExtension#close`（main）

```java
// release（Java 17）                              // release-1.x（Java 8）
if (component instanceof AutoCloseable closeable) {   if (component instanceof AutoCloseable) {
                                                        AutoCloseable closeable = (AutoCloseable) component;
    closeable.close();                                closeable.close();
}                                                   }
```

用经典强转替代模式匹配 `instanceof`；按资源清理语义（FOR_EACH 与 FOR_ALL 作用域的选择）
不变。

### 2. `MyBatisTestExtensionTest`（测试夹具漂移）

1.x 的测试类额外声明了若干 `@MyBatisRuntime private static …` 字段
（`Configuration`、`Environment`、`SqlSessionFactory`、`DataSource`、`Properties`、
`SqlSession`、`Transaction`）。这正好演练了文档化的规则：**不支持静态注入** 的 resolver
（例如 `SqlSessionResolver`）会把字段留为 `null` 而不报错 —— 与 current 文档分析的
`StaticFieldTests` 断言一致。

## 版本线事实

| 方面 | 1.x |
|---|---|
| JUnit Jupiter | 版本来自 0.1.26 上游代际（`microsphere-spring-cloud-parent` / 其 BOM），不由本仓库管理 |
| H2 | test 作用域的夹具引擎角色不变（`jdbc:h2:mem:test_mem`） |

## 另见

- [1.x 差异总览](README.md)
- [用户文档](../../../user-guide/1.x/zh/test.md)
