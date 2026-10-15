# microsphere-mybatis-test —— 用户文档

[English](../test.md) · [开发者文档](../../../developer-guide/current/zh/test.md) · [← 索引](README.md)

可复用的 MyBatis 测试基础设施：

1. **JUnit 5 扩展**（`@MyBatisTest` + `@MyBatisRuntime`）：把运行中的 MyBatis 运行时
   （`Configuration`、`SqlSession`、`Executor`、Mapper 等）注入字段与方法参数。
2. **抽象测试基类体系**（`AbstractMyBatisTest` → `AbstractSqlSessionTest` →
   `AbstractExecutorTest` → `AbstractMapperTest`），内置数据库生命周期。
3. **固件资源** —— 随 `src/main/resources` 发布的 H2 `config.xml`、示例实体
   （`User`、`Father`、`Child`）、XML Mapper 与 `init-db.sql` / `destroy-db.sql` 脚本。

## 版本信息

| 项目 | 值 |
|---|---|
| 构件 | `io.github.microsphere-projects:microsphere-mybatis-test` |
| 文档对应版本 | `0.2.16`（`release` 分支，0.2.x 线） |
| Java 基线 | 17 |
| 依赖 | `microsphere-mybatis-utils`；optional：MyBatis `3.5.19`、JUnit Jupiter、H2 `2.3.232`、logback |
| 兼容性 | 使用 JUnit 5（Jupiter）扩展回调 + `ParameterResolver` API |

上游依赖均为 `optional`，请在测试 scope 中自行添加：

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

## API 说明 —— JUnit 5 扩展

### `@MyBatisTest`（类型级，`@Inherited`）

| 属性 | 默认值 | 含义 |
|---|---|---|
| `configResource` | `META-INF/mybatis/config.xml` | XML 配置资源 |
| `environment` | `testing` | 选择的 `<environment id=...>` |
| `propertiesResource` | `META-INF/mybatis/mybatis.properties` | `${...}` 变量替换属性 |

`@MyBatisTest` 已元标注 `@ExtendWith(MyBatisTestExtension.class)`。

### `@MyBatisRuntime`（字段 / 参数标记）

支持矩阵（由解析器 SPI 强制约束）：

| 组件 | static 字段 | 实例字段 | 方法参数 |
|---|---|---|---|
| `Configuration`、`Environment`、`SqlSessionFactory`、`DataSource`、`Properties` | 支持 | 支持 | 支持 |
| `SqlSession`、`Connection`、`Transaction`、`Executor`、任意已注册 Mapper | **不支持（保持 `null`）** | 支持 | 支持 |

### `MyBatisTestExtension` 生命周期

| 回调 | 行为 |
|---|---|
| `beforeAll` | 解析 `Configuration`（同时存入 `Properties`），注入标注过的 **static** 字段 |
| `postProcessTestInstance` | 注入标注过的**实例**字段（没有 `BeforeEachCallback`） |
| `afterEach` | 关闭 FOR_EACH 存储中的单次测试组件（`SqlSession`、`Connection` 等） |
| `afterAll` | 关闭 FOR_ALL 存储中的共享组件 |

组件按 `ExtensionContext.Store` 缓存；Mapper 接口通过 `Configuration` 的 `MapperRegistry`
匹配而非精确类型。

### 解析器 SPI（`io.microsphere.mybatis.test.junit.jupiter.resolver`）

10 个 `ComponentResolver` 实现通过
`META-INF/services/io.microsphere.mybatis.test.junit.jupiter.resolver.ComponentResolver`
（ServiceLoader）发现；追加该文件即可贡献自定义解析器。

| 解析器 | 提供类型 | 支持 static |
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
| `MapperComponentResolver` | 任意已注册 Mapper 接口 | ✘ |

### 使用示例（摘自 `MyBatisTestExtensionTest`）

```java
@MyBatisTest                      // 全部使用默认属性
class MyBatisTestExtensionTest {

    class FieldTests {
        @MyBatisRuntime private Configuration configuration;
        @MyBatisRuntime private SqlSession sqlSession;
        @MyBatisRuntime private Executor executor;
        @MyBatisRuntime private UserMapper userMapper;

        @Test
        void test() throws Exception {
            runScript(dataSource, INIT_DB_SCRIPT_RESOURCE_NAME);  // 数据不会自动初始化
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

> 注意：与抽象基类方式不同，扩展**不会**自动执行 `init-db.sql`，
> 需自行调用 `MyBatisUtils.runScript(...)`（或继承 `AbstractMyBatisTest`）。

## API 说明 —— 抽象测试基类体系

```
AbstractMyBatisTest
  └─ AbstractSqlSessionTest   (@Test testSqlSession)
       └─ AbstractExecutorTest(@Test testExecutor；MS_ID_* 常量)
            └─ AbstractMapperTest(@Test testMapper；assertUserMapper/ChildMapper/FatherMapper)
```

继承最末派生类即可一次执行全部继承测试：

```java
class AbstractMapperTestTest extends AbstractMapperTest {
}   // 针对 H2 依次执行 testSqlSession + testExecutor + testMapper
```

| 成员（`AbstractMyBatisTest`） | 说明 |
|---|---|
| `@BeforeEach init()` | 构建默认 `SqlSessionFactory`，回调 `customize(SqlSessionFactory)` 与 `customize(Configuration)`，执行 `init-db.sql` |
| `@AfterEach destroy()` | 执行 `destroy-db.sql` |
| `customize(Configuration)` | 覆写钩子——如 `configuration.addInterceptor(...)`（core 模块测试即如此使用） |
| `doInSqlSession / doInExecutor / doInMapper(Class, consumer)` | 异常安全的运行模板 |
| `runSafely(ThrowableAction)` | 执行并吞掉/记录失败（负向测试） |
| `getMappedStatement(id)`、`getConnection(Executor)`、`getTransaction(Executor)`、`getDataSource()`、`getEnvironment()`、`getConfiguration()` | 访问器 |
| `static User createUser()` | 随机 id 固件 |
| `static assertConfiguration(Configuration)` | 校验别名 `user`/`child`/`father` 与 Mapper 注册 |

## API 说明 —— `MyBatisTestUtils`

| 常量 | 值 |
|---|---|
| `DEFAULT_CONFIG_RESOURCE_NAME` | `META-INF/mybatis/config.xml` |
| `EMPTY_CONFIG_RESOURCE_NAME` | `META-INF/mybatis/empty-config.xml` |
| `DEFAULT_ENVIRONMENT_ID` | `testing` |
| `DEFAULT_PROPERTIES_RESOURCE_NAME` | `META-INF/mybatis/mybatis.properties` |
| `INIT_DB_SCRIPT_RESOURCE_NAME` | `META-INF/sql/init-db.sql` |
| `DESTROY_DB_SCRIPT_RESOURCE_NAME` | `META-INF/sql/destroy-db.sql` |
| `USER_TYPE_ALIAS` / `CHILD_TYPE_ALIAS` / `FATHER_TYPE_ALIAS` | `user` / `child` / `father` |

| 静态方法 | 说明 |
|---|---|
| `loadDefaultProperties()` | 加载默认属性资源 |
| `getDefaultConfiguration()` | 以默认环境/属性解析默认配置 |
| `getDefaultDataSource()` / `getDefaultEnvironment()` | 从默认配置获取 |
| `buildDefaultSqlSessionFactory()` | 默认配置上的默认工厂 |
| `runCreateDatabaseScript(DataSource)` / `runDestroyDatabaseScript(DataSource)` | 一行执行 init/destroy 脚本 |

## 随包发布的固件

| 资源 | 内容 |
|---|---|
| `META-INF/mybatis/config.xml` | `POOLED` H2 数据源（`jdbc:h2:mem:test_mem`）、`JDBC` 事务管理器、别名、三个 Mapper、`lazyLoadingEnabled=false` |
| `META-INF/mybatis/mybatis.properties` | `jdbc.driver=org.h2.Driver` 等 |
| `META-INF/mybatis/mappers/*.xml` | `UserMapper.xml`（含 `<cache/>` 二级缓存与故意写坏表的 `getErrorUserByName`）、`ChildMapper.xml`（懒加载 `<association select=...>` 到 Father）、`FatherMapper.xml` |
| `META-INF/sql/init-db.sql` | 建 `users`、`child`、`father` 表；植入 father#1 "John Smith" 与 children#1–2 |
| `META-INF/sql/destroy-db.sql` | 删除三张表 |

固件 Mapper 是纯接口——全部 SQL 在 XML 中；在自己的测试里复用
（`UserMapper`、`ChildMapper`、`FatherMapper`、`User`、`Father`、`Child`）即是设计意图。

## 另见

- [spring-test 模块](spring-test.md) —— 同样的固件包装为 Spring 配置
- [开发者文档：test](../../../developer-guide/current/zh/test.md) —— 扩展生命周期与解析器依赖图
