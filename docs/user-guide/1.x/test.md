# microsphere-mybatis-test — User Guide (1.x / 0.1.x)

[中文](zh/test.md) · [Developer Guide](../../developer-guide/1.x/test.md) · [← 1.x Index](README.md)

The JUnit 5 extension (`@MyBatisTest` / `@MyBatisRuntime`), the abstract test hierarchy and the
H2 fixture resources are the same in 0.1.x as in `current` — see
[current/test.md](../current/test.md) for the full reference. 1.x specifics:

## Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-mybatis-test` |
| Documented version | `0.1.16` (branch `release-1.x`, 0.1.x line) |
| Java baseline | **8** |
| Requires | `microsphere-mybatis-utils`; optional: MyBatis `3.5.19`, JUnit Jupiter 5.x, H2 (2.x), logback |

## 1.x Notes

- **JUnit 5 (Jupiter) only.** For JUnit 4, this module offers nothing — the abstract classes
  (`AbstractMyBatisTest` and subclasses) use `@BeforeEach`/`@AfterEach`/`@Test` from
  `org.junit.jupiter.api`.
- `MyBatisTestExtension` in 0.1.x contains a classic `instanceof AutoCloseable` cast where
  `current` uses pattern matching — behaviour identical.
- The resolver SPI file ships at
  `META-INF/services/io.microsphere.mybatis.test.junit.jupiter.resolver.ComponentResolver`
  with the same ten resolvers, same order, same static/instance support matrix.
- Consumer wiring (test scope):

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-mybatis-test</artifactId>
    <version>0.1.16</version>
    <scope>test</scope>
</dependency>
```

## Usage Recap (verbatim from the module's tests)

```java
@MyBatisTest
class MyDaoTest {

    @MyBatisRuntime
    private UserMapper userMapper;   // instance field injection

    @Test
    void test(@MyBatisRuntime SqlSession sqlSession) {
        assertNotNull(sqlSession);
    }
}
```

## See Also

- [Full reference (current)](../current/test.md) — attribute tables, resolver list, fixture inventory
