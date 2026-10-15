# microsphere-mybatis-test — User Guide

[中文](zh/test.md) · [Developer Guide](../../developer-guide/current/test.md) · [← Index](../README.md)

Reusable test infrastructure for MyBatis projects:

1. A **JUnit 5 extension** (`@MyBatisTest` + `@MyBatisRuntime`) that injects a live MyBatis
   runtime (`Configuration`, `SqlSession`, `Executor`, mappers, …) into fields and parameters.
2. An **abstract test class hierarchy** (`AbstractMyBatisTest` → `AbstractSqlSessionTest` →
   `AbstractExecutorTest` → `AbstractMapperTest`) with built-in database lifecycle.
3. **Fixture resources** — an H2-backed `config.xml`, sample entities (`User`, `Father`, `Child`),
   XML mappers and `init-db.sql` / `destroy-db.sql` scripts shipped in `src/main/resources`.

## Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-mybatis-test` |
| Documented version | `0.2.16` (branch `release`, 0.2.x line) |
| Java baseline | 17 |
| Requires | `microsphere-mybatis-utils`; optional: MyBatis `3.5.19`, JUnit Jupiter, H2 `2.3.232`, logback |
| Compatibility | JUnit 5 (Jupiter) 5.x/6.x API surface used: extension callbacks + `ParameterResolver` |

Because the upstream dependencies are `optional`, add them in your test scope:

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-mybatis-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

## API Reference — JUnit 5 Extension

### `@MyBatisTest` (type-level, `@Inherited`)

| Attribute | Default | Meaning |
|---|---|---|
| `configResource` | `META-INF/mybatis/config.xml` (`MyBatisTestUtils.DEFAULT_CONFIG_RESOURCE_NAME`) | XML configuration resource |
| `environment` | `testing` (`DEFAULT_ENVIRONMENT_ID`) | Selected `<environment id=...>` |
| `propertiesResource` | `META-INF/mybatis/mybatis.properties` (`DEFAULT_PROPERTIES_RESOURCE_NAME`) | Variables for `${...}` substitution |

`@MyBatisTest` is meta-annotated with `@ExtendWith(MyBatisTestExtension.class)`.

### `@MyBatisRuntime` (field / parameter marker)

Support matrix (also enforced by the resolver SPI):

| Component | static field | instance field | method parameter |
|---|---|---|---|
| `Configuration`, `Environment`, `SqlSessionFactory`, `DataSource`, `Properties` | yes | yes | yes |
| `SqlSession`, `Connection`, `Transaction`, `Executor`, any registered Mapper | **no (left `null`)** | yes | yes |

### `MyBatisTestExtension` lifecycle

| Callback | Effect |
|---|---|
| `beforeAll` | Resolves `Configuration` (which also stores the loaded `Properties`), injects annotated **static** fields |
| `postProcessTestInstance` | Injects annotated **instance** fields (there is no `BeforeEachCallback`) |
| `afterEach` | Closes per-test components (`SqlSession`, `Connection`, …) from the FOR_EACH store |
| `afterAll` | Closes shared components from the FOR_ALL store |

Components are cached per `ExtensionContext.Store`; mapper interfaces are matched by the
`Configuration`'s `MapperRegistry` rather than by exact type.

### Resolver SPI (`io.microsphere.mybatis.test.junit.jupiter.resolver`)

Ten `ComponentResolver` implementations are discovered through
`META-INF/services/io.microsphere.mybatis.test.junit.jupiter.resolver.ComponentResolver`
(ServiceLoader). You can contribute your own resolver by appending to that file.

| Resolver | Type provided | Static-capable |
|---|---|---|
| `ConfigurationResolver` | `Configuration` | ✔ |
| `EnvironmentResolver` | `Environment` | ✔ |
| `SqlSessionFactoryResolver` | `SqlSessionFactory` | ✔ |
| `DataSourceResolver` | `DataSource` | ✔ |
| `PropertiesResolver` | `Properties` | ✔ |
| `SqlSessionResolver` | `SqlSession` | ✘ |
| `ConnectionResolver` | `Connection` | ✘ |
| `TransactionResolver` | `Transaction` | ✘ |
| `ExecutorResolver` | `Executor` | ✘ |
| `MapperComponentResolver` | any registered mapper interface | ✘ |

### Usage Examples (verbatim from `MyBatisTestExtensionTest`)

```java
@MyBatisTest                      // all attributes at defaults
class MyBatisTestExtensionTest {

    class FieldTests {
        @MyBatisRuntime private Configuration configuration;
        @MyBatisRuntime private SqlSession sqlSession;
        @MyBatisRuntime private Executor executor;
        @MyBatisRuntime private UserMapper userMapper;

        @Test
        void test() throws Exception {
            runScript(dataSource, INIT_DB_SCRIPT_RESOURCE_NAME);  // data is NOT auto-initialized
            assertUserMapper(userMapper);
            runScript(dataSource, DESTROY_DB_SCRIPT_RESOURCE_NAME);
        }
    }

    class MethodTests {
        @Test
        void test(@MyBatisRuntime Configuration configuration,
                  @MyBatisRuntime SqlSession sqlSession,
                  @MyBatisRuntime UserMapper userMapper) {
            assertNotNull(userMapper);
        }
    }
}
```

> Note: unlike the abstract-class style, the extension does **not** execute `init-db.sql`
> automatically — call `MyBatisUtils.runScript(...)` (or extend `AbstractMyBatisTest`).

## API Reference — Abstract Test Hierarchy

```
AbstractMyBatisTest
  └─ AbstractSqlSessionTest   (@Test testSqlSession)
       └─ AbstractExecutorTest(@Test testExecutor; MS_ID_* constants)
            └─ AbstractMapperTest(@Test testMapper; assertUserMapper/ChildMapper/FatherMapper)
```

Extending the most derived class runs all inherited tests in one go:

```java
class AbstractMapperTestTest extends AbstractMapperTest {
}   // executes testSqlSession + testExecutor + testMapper against H2
```

| Member (of `AbstractMyBatisTest`) | Description |
|---|---|
| `@BeforeEach init()` | Builds the default `SqlSessionFactory`, calls `customize(SqlSessionFactory)` and `customize(Configuration)`, then runs `init-db.sql` |
| `@AfterEach destroy()` | Runs `destroy-db.sql` |
| `customize(Configuration)` | Override hook — e.g. `configuration.addInterceptor(...)` (used by the core module's tests) |
| `doInSqlSession / doInExecutor / doInMapper(Class, consumer)` | Throwable-safe templates around the runtime |
| `runSafely(ThrowableAction)` | Executes and swallows/logs failures (negative testing) |
| `getMappedStatement(String id)`, `getConnection(Executor)`, `getTransaction(Executor)`, `getDataSource()`, `getEnvironment()`, `getConfiguration()` | Accessors |
| `static User createUser()` | Random-id fixture |
| `static assertConfiguration(Configuration)` | Validates aliases `user`/`child`/`father` and mapper registration |

## API Reference — `MyBatisTestUtils`

| Constant | Value |
|---|---|
| `DEFAULT_CONFIG_RESOURCE_NAME` | `META-INF/mybatis/config.xml` |
| `EMPTY_CONFIG_RESOURCE_NAME` | `META-INF/mybatis/empty-config.xml` |
| `DEFAULT_ENVIRONMENT_ID` | `testing` |
| `DEFAULT_PROPERTIES_RESOURCE_NAME` | `META-INF/mybatis/mybatis.properties` |
| `INIT_DB_SCRIPT_RESOURCE_NAME` | `META-INF/sql/init-db.sql` |
| `DESTROY_DB_SCRIPT_RESOURCE_NAME` | `META-INF/sql/destroy-db.sql` |
| `USER_TYPE_ALIAS` / `CHILD_TYPE_ALIAS` / `FATHER_TYPE_ALIAS` | `user` / `child` / `father` |

| Method (all `static`) | Description |
|---|---|
| `loadDefaultProperties()` | Loads the default properties resource |
| `getDefaultConfiguration()` | Parses the default config with the default environment/properties |
| `getDefaultDataSource()` / `getDefaultEnvironment()` | From the default configuration |
| `buildDefaultSqlSessionFactory()` | Default factory over the default configuration |
| `runCreateDatabaseScript(DataSource)` / `runDestroyDatabaseScript(DataSource)` | init/destroy scripts as one calls |

## Shipped Fixtures

| Resource | Content |
|---|---|
| `META-INF/mybatis/config.xml` | `POOLED` H2 datasource (`jdbc:h2:mem:test_mem`), `JDBC` transaction manager, aliases, three mappers, `lazyLoadingEnabled=false` |
| `META-INF/mybatis/mybatis.properties` | `jdbc.driver=org.h2.Driver` etc. |
| `META-INF/mybatis/mappers/*.xml` | `UserMapper.xml` (with `<cache/>` second-level cache and a deliberately broken `getErrorUserByName`), `ChildMapper.xml` (lazy `<association select=...>` to Father), `FatherMapper.xml` |
| `META-INF/sql/init-db.sql` | Creates `users`, `child`, `father`; seeds father#1 "John Smith" and children#1–2 |
| `META-INF/sql/destroy-db.sql` | Drops the three tables |

The fixture mappers are pure interfaces — all SQL lives in the XML files; reusing them
(`UserMapper`, `ChildMapper`, `FatherMapper`, `User`, `Father`, `Child`) in your own tests is
the intended usage.

## See Also

- [spring-test module](spring-test.md) — the same fixtures as Spring configurations
- [Developer Guide: test](../../developer-guide/current/test.md) — extension lifecycle & resolver graphs
