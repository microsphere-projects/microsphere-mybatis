# microsphere-mybatis-spring-test — User Guide (1.x / 0.1.x)

[中文](zh/spring-test.md) · [Developer Guide](../../developer-guide/1.x/spring-test.md) · [← 1.x Index](README.md)

The three fixture configurations (`MyBatisDataSourceTestConfiguration`,
`MyBatisDataBaseTestConfiguration`, `MyBatisTestConfiguration`) are identical to the 0.2.x line —
see [current/spring-test.md](../current/spring-test.md) for bean tables and examples.

## Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-mybatis-spring-test` |
| Documented version | `0.1.16` (branch `release-1.x`, 0.1.x line) |
| Java baseline | **8** |
| Requires | `microsphere-mybatis-test` 0.1.16; optional: MyBatis-Spring **2.1.2**, Spring **5.x**, H2, JUnit Jupiter |

## 1.x Notes

- `MyBatisTestConfiguration` builds the `SqlSessionFactory` with
  `org.mybatis.spring.SqlSessionFactoryBean` from **mybatis-spring 2.1.2** — same class name,
  older artifact than the 4.1.0 used on `current`.
- `@EventListener` database lifecycle in `MyBatisDataBaseTestConfiguration` works on Spring 5
  unchanged.
- Remember the pairing rule: import `MyBatisDataBaseTestConfiguration` explicitly when you need
  `init-db.sql` to run (as in this line's `EnableMyBatisTest`).

```java
@EnableMyBatis
@Import(value = {
        MyBatisDataSourceTestConfiguration.class,
        MyBatisDataBaseTestConfiguration.class
})
static class DefaultConfig {
}
```

## See Also

- [current/spring-test.md](../current/spring-test.md) — full bean reference
