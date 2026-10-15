# microsphere-mybatis-spring-test — User Guide

[中文](zh/spring-test.md) · [Developer Guide](../../developer-guide/current/spring-test.md) · [← Index](../README.md)

Three drop-in Spring `@Configuration` classes that expose the
[microsphere-mybatis-test](test.md) fixtures (H2 `config.xml`, init/destroy scripts) as Spring
beans, so container-based tests need zero custom wiring.

## Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-mybatis-spring-test` |
| Documented version | `0.2.16` (branch `release`, 0.2.x line) |
| Java baseline | 17 |
| Requires | `microsphere-mybatis-test`; optional: Spring 6.x, MyBatis-Spring `4.1.0`, H2 `2.3.232`, JUnit Jupiter |

## API Reference

### `MyBatisDataSourceTestConfiguration`

| Bean | Type | Notes |
|---|---|---|
| `configuration` | `org.apache.ibatis.session.Configuration` | Parsed from `META-INF/mybatis/config.xml` + `testing` environment via `MyBatisTestUtils.getDefaultConfiguration()` |
| `dataSource` (@Primary, `destroyMethod = "forceCloseAll"`) | `DataSource` | The MyBatis-managed `POOLED` H2 `PooledDataSource` from the environment |

### `MyBatisDataBaseTestConfiguration`

Declares no beans; manages the **schema lifecycle** per application context:

- `@EventListener(ContextRefreshedEvent)` → runs `META-INF/sql/init-db.sql`
- `@EventListener(ContextClosedEvent)` → runs `META-INF/sql/destroy-db.sql`

### `MyBatisTestConfiguration`

`@Import`s `MyBatisDataSourceTestConfiguration` and adds:

| Bean | Type | Notes |
|---|---|---|
| `sqlSessionFactory` | `SqlSessionFactory` | Built by `org.mybatis.spring.SqlSessionFactoryBean` from the fixture `configuration` + `dataSource` |
| `childMapper` / `fatherMapper` / `userMapper` | mapper proxies | `getMapper(...)` from the factory |

> `MyBatisTestConfiguration` does **not** import `MyBatisDataBaseTestConfiguration` — tables are
> not created unless you import it explicitly.

## Usage Examples (verbatim from this module's and the spring module's tests)

```java
@SpringJUnitConfig(classes = {
        MyBatisTestConfiguration.class,
        MyBatisTestConfigurationTest.class
})
class MyBatisTestConfigurationTest {

    @Autowired private SqlSessionFactory sqlSessionFactory;
    @Autowired private ChildMapper childMapper;
    @Autowired private FatherMapper fatherMapper;
    @Autowired private UserMapper userMapper;

    @Test
    void test() {
        assertNotNull(sqlSessionFactory);
        assertNotNull(userMapper);
    }
}
```

The canonical pairing used by `microsphere-mybatis-spring` tests — datasource + schema lifecycle:

```java
@SpringJUnitConfig(classes = {
        MyBatisDataBaseTestConfiguration.class,
        MyBatisDataSourceTestConfiguration.class,
        MyBatisDataBaseTestConfigurationTest.class
})
class MyBatisDataBaseTestConfigurationTest { ... }
```

```java
// With @EnableMyBatis (spring module)
@EnableMyBatis
@Import(value = {
        MyBatisDataSourceTestConfiguration.class,
        MyBatisDataBaseTestConfiguration.class
})
static class DefaultConfig { }
```

## See Also

- [spring module](spring.md) · [test module](test.md)
- [Developer Guide: spring-test](../../developer-guide/current/spring-test.md)
