# microsphere-mybatis-spring-test — Developer Guide (`current`)

[中文](zh/spring-test.md) · [← Index](README.md) · [User Guide](../../user-guide/current/spring-test.md)

Three `@Configuration(proxyBeanMethods = false)` classes that repackage the plain-test fixtures
as Spring beans. Thin by design: no conditions, no profiles, no custom namespaces.

## Architecture Role

```mermaid
graph LR
    MTU[MyBatisTestUtils\n(test module)] --> DSTC[MyBatisDataSourceTestConfiguration]
    DSTC -->|@Import| MTC[MyBatisTestConfiguration]
    DSTC -->|shares DataSource| DBTC[MyBatisDataBaseTestConfiguration]
    DSTC --> SPRING["@EnableMyBatis\n(spring module tests)"]
    DBTC --> SPRING
```

## Source Analysis

### `MyBatisDataSourceTestConfiguration`

```java
@Bean
public org.apache.ibatis.session.Configuration configuration() throws IOException {
    return MyBatisTestUtils.getDefaultConfiguration();
}

@Bean(destroyMethod = "forceCloseAll")
@Primary
public DataSource dataSource(org.apache.ibatis.session.Configuration configuration) {
    return configuration.getEnvironment().getDataSource();
}
```

Two deliberate quirks:

1. The `Configuration` bean type is written fully qualified — inside a Spring `@Configuration`
   class, the unqualified name would collide with the annotation's own `Configuration` import.
2. The exposed `DataSource` is MyBatis' `PooledDataSource` **owned by the fixture
   `Environment`**, not a Spring-created pool. Hence `destroyMethod = "forceCloseAll"` —
   that method exists only on `PooledDataSource` (`DataSource` has no close contract), and it
   forcibly returns checked-out connections when the context shuts down. `@Primary` lets
   `@EventListener` and `SqlSessionFactoryBean` autowire it without ambiguity.

### `MyBatisDataBaseTestConfiguration`

No beans — pure lifecycle observer:

```java
@EventListener(ContextRefreshedEvent.class) → runCreateDatabaseScript(context.getBean(DataSource.class))
@EventListener(ContextClosedEvent.class)   → runDestroyDatabaseScript(...)
```

Scope difference vs the JUnit extension path: schema lives **per application context**, not
per test method. All `@SpringJUnitConfig` tests in one context share tables; tests needing a
clean slate need `@DirtiesContext` or manual scripts. This class is NOT imported by
`MyBatisTestConfiguration` — schema creation is opt-in (see `EnableMyBatisTest.DefaultConfig`
which imports both).

### `MyBatisTestConfiguration`

Wraps `org.mybatis.spring.SqlSessionFactoryBean` manually:

```java
SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
bean.setConfiguration(configuration);
bean.setDataSource(dataSource);
return bean.getObject();
```

`getObject()` is called directly (the config is not a `FactoryBean` itself), which builds the
factory eagerly at bean-creation time. The three mapper beans come from a package-private
`getMapper` helper that opens a session in try-with-resources and returns the proxy after the
session closes — safe because MyBatis mapper proxies re-open sessions per statement via the
factory.

## Design Patterns

| Pattern | Where |
|---|---|
| Facade | fixture classes hide MyBatis/H2 details behind standard bean types |
| Observer | `@EventListener` on context events for DB lifecycle |
| Composition over inheritance | `MyBatisTestConfiguration` `@Import`s the datasource config instead of extending it |

## See Also

- [test analysis](test.md) — where the fixtures come from
- [spring analysis](spring.md) — the canonical consumer pairings
