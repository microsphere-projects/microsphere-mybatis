# microsphere-mybatis-utils — User Guide

[中文](zh/utils.md) · [Developer Guide](../../developer-guide/current/utils.md) · [← Index](../README.md)

Static utility methods to bootstrap and drive MyBatis **without any container**: loading
properties and XML configuration, building `Configuration` / `SqlSessionFactory`, creating
`Connection`, `Transaction` and `Executor` instances, and running SQL scripts.

## Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-mybatis-utils` |
| Documented version | `0.2.16` (branch `release`, 0.2.x line) |
| Java baseline | 17 |
| Requires | MyBatis `3.5.19` (declared `optional` — provide it yourself) |
| API stability | All types are `@since 1.0.0`; the 0.2.x line only adds javadoc/dependency updates |

> The published BOM (`microsphere-mybatis-dependencies`) does **not** manage this artifact's
> version. Depend on it transitively via `microsphere-mybatis-core`, or pin the version yourself.

## Installation

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-mybatis-utils</artifactId>
    <version>0.2.16</version>
</dependency>
```

## API Reference — `io.microsphere.mybatis.util.MyBatisUtils`

`MyBatisUtils` is an abstract final-style utility class (private constructor, all methods static).

### Bootstrap

| Method | Description |
|---|---|
| `static Properties loadProperties(String propertiesResource) throws IOException` | Loads a classpath `.properties` resource (e.g. `META-INF/mybatis/mybatis.properties`) |
| `static Configuration getConfiguration(String configResource) throws IOException` | Parses an XML config with no environment / properties |
| `static Configuration getConfiguration(String configResource, String environment) throws IOException` | Parses selecting a named `<environment>` |
| `static Configuration getConfiguration(String configResource, String environment, Properties properties) throws IOException` | Parses with variable substitution (wraps MyBatis `XMLConfigBuilder`) |
| `static SqlSessionFactory buildSqlSessionFactory(Configuration configuration)` | Returns a `DefaultSqlSessionFactory` for the configuration |
| `static SqlSession openSession(Configuration configuration)` | Convenience: factory + `openSession()` — the caller must close the session |

### Component Access

| Method | Description |
|---|---|
| `static Environment getEnvironment(Configuration configuration)` | Reads `configuration.getEnvironment()` |
| `static DataSource getDataSource(Configuration configuration)` / `getDataSource(Environment environment)` | The environment's `DataSource` |
| `static Connection getConnection(Configuration configuration)` | Opens a session and returns its connection (**not** auto-closed) |
| `static Transaction newTransaction(Configuration configuration, Connection connection)` | Creates a `Transaction` via the environment's `TransactionFactory` |
| `static Executor newExecutor(Configuration configuration, Connection connection)` | `configuration.newExecutor(transaction)` — honours the configured executor type and plugins |

### Script Execution

| Method | Description |
|---|---|
| `static void runScript(DataSource dataSource, String scriptResource) throws IOException, SQLException` | Runs a DDL/DML script with `ScriptRunner` (auto-commit on, stop-on-error off, silent logging) |
| `static void runScript(ScriptRunner runner, String resource) throws IOException` | Runs a script through a pre-configured runner |

### Callback Templates (try-with-resources semantics)

| Method | Description |
|---|---|
| `static void doInSqlSession(Configuration, ThrowableConsumer<SqlSession>) throws Throwable` | Opens/closes a `SqlSession` around the consumer |
| `static void doInConnection(Configuration, ThrowableConsumer<Connection>) throws Throwable` | Also closes the connection |
| `static void doInExecutor(Configuration, ThrowableConsumer<Executor>) throws Throwable` | Creates an `Executor` with a fresh transaction and closes it (`close(false)`) afterwards |

The `ThrowableConsumer` lambdas may throw any checked exception; it propagates as `Throwable`.

## Code Example

Matching the class javadoc and `microsphere-mybatis-test` fixtures (H2 in-memory database):

```java
// 1. Load properties and parse the XML configuration
Properties props = MyBatisUtils.loadProperties("META-INF/mybatis/mybatis.properties");
Configuration configuration =
    MyBatisUtils.getConfiguration("META-INF/mybatis/config.xml", "testing", props);

// 2. Initialize the schema
MyBatisUtils.runScript(MyBatisUtils.getDataSource(configuration), "META-INF/sql/init-db.sql");

// 3. Work directly with the Executor (transaction + statement access)
MyBatisUtils.doInExecutor(configuration, executor -> {
    Transaction transaction = executor.getTransaction();
    MappedStatement ms = configuration.getMappedStatement(
        "io.microsphere.mybatis.test.mapper.UserMapper.saveUser");
    ms.getBoundSql(new User(1, "Mercy"));
    executor.update(ms, new User(1, "Mercy"));
    transaction.commit();
});

// 4. Or via a SqlSession and mapper
MyBatisUtils.doInSqlSession(configuration, sqlSession -> {
    UserMapper mapper = sqlSession.getMapper(UserMapper.class);
    mapper.saveUser(new User(2, "John Smith"));
    sqlSession.commit();
});

// 5. Clean up
MyBatisUtils.runScript(MyBatisUtils.getDataSource(configuration), "META-INF/sql/destroy-db.sql");
```

## Typical `config.xml` Consumed by These APIs

```xml
<configuration>
    <settings>
        <setting name="lazyLoadingEnabled" value="false"/>
    </settings>
    <typeAliases>
        <typeAlias type="...entity.User" alias="user"/>
    </typeAliases>
    <environments default="testing">
        <environment id="testing">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver"   value="${jdbc.driver}"/>
                <property name="url"      value="${jdbc.url}"/>
                <property name="username" value="${jdbc.username}"/>
                <property name="password" value="${jdbc.password}"/>
            </dataSource>
        </environment>
    </environments>
    <mappers>
        <mapper resource="META-INF/mybatis/mappers/UserMapper.xml"/>
    </mappers>
</configuration>
```

## See Also

- [microsphere-mybatis-core](core.md) — builds on these utilities with the Executor filter chain
- [microsphere-mybatis-test](test.md) — ships ready-made fixture resources used above
