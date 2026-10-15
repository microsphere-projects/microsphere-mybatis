# microsphere-mybatis-test (1.x) — Developer Guide

[中文](zh/test.md) · [← 1.x Index](README.md) · [Current analysis](../current/test.md)

The JUnit 5 extension, resolver SPI, abstract hierarchy, and fixtures are the same design —
see [current/test.md](../current/test.md) for lifecycle diagrams and the store-key caching
subtlety. Two files differ on this branch.

## Source diffs

### 1. `MyBatisTestExtension#close` (main)

```java
// release (Java 17)                              // release-1.x (Java 8)
if (component instanceof AutoCloseable closeable) {   if (component instanceof AutoCloseable) {
                                                        AutoCloseable closeable = (AutoCloseable) component;
    closeable.close();                                closeable.close();
}                                                   }
```

Classic cast instead of pattern-matching `instanceof`; per-resource cleanup semantics
(FOR_EACH vs FOR_ALL scope selection) unchanged.

### 2. `MyBatisTestExtensionTest` (test fixture drift)

The 1.x test class declares extra `@MyBatisRuntime private static …` fields
(`Configuration`, `Environment`, `SqlSessionFactory`, `DataSource`, `Properties`,
`SqlSession`, `Transaction`). This exercises the documented rule that **unsupported-static**
resolvers (e.g. `SqlSessionResolver`) leave the field `null` rather than failing —
consistent with the `StaticFieldTests` assertions analysed in the current doc.

## Line facts

| Aspect | 1.x |
|---|---|
| JUnit Jupiter | version comes from the 0.1.26 upstream generation (`microsphere-spring-cloud-parent` / its BOM), not from this repo |
| H2 | test-scope fixture engine unchanged in role (`jdbc:h2:mem:test_mem`) |

## See Also

- [1.x differences overview](README.md)
- [User Guide](../../user-guide/1.x/test.md)
