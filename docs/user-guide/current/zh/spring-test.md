# microsphere-mybatis-spring-test —— 用户文档

[English](../spring-test.md) · [开发者文档](../../../developer-guide/current/zh/spring-test.md) · [← 索引](README.md)

三个可直接使用的 Spring `@Configuration` 类，把
[microsphere-mybatis-test](test.md) 固件（H2 `config.xml`、init/destroy 脚本）暴露为
Spring Bean，让基于容器的测试零装配。

## 版本信息

| 项目 | 值 |
|---|---|
| 构件 | `io.github.microsphere-projects:microsphere-mybatis-spring-test` |
| 文档对应版本 | `0.2.16`（`release` 分支，0.2.x 线） |
| Java 基线 | 17 |
| 依赖 | `microsphere-mybatis-test`；optional：Spring 6.x、MyBatis-Spring `4.1.0`、H2 `2.3.232`、JUnit Jupiter |

## API 说明

### `MyBatisDataSourceTestConfiguration`

| Bean | 类型 | 说明 |
|---|---|---|
| `configuration` | `org.apache.ibatis.session.Configuration` | 经 `MyBatisTestUtils.getDefaultConfiguration()` 解析 `META-INF/mybatis/config.xml` + `testing` 环境 |
| `dataSource`（@Primary，`destroyMethod = "forceCloseAll"`） | `DataSource` | environment 中 MyBatis 管理的 `POOLED` H2 `PooledDataSource` |

### `MyBatisDataBaseTestConfiguration`

不声明 Bean；按**应用上下文**管理表结构生命周期：

- `@EventListener(ContextRefreshedEvent)` → 执行 `META-INF/sql/init-db.sql`
- `@EventListener(ContextClosedEvent)` → 执行 `META-INF/sql/destroy-db.sql`

### `MyBatisTestConfiguration`

`@Import` `MyBatisDataSourceTestConfiguration` 并追加：

| Bean | 类型 | 说明 |
|---|---|---|
| `sqlSessionFactory` | `SqlSessionFactory` | 由 `org.mybatis.spring.SqlSessionFactoryBean` 基于固件 `configuration` + `dataSource` 构建 |
| `childMapper` / `fatherMapper` / `userMapper` | Mapper 代理 | 由工厂 `getMapper(...)` 获得 |

> `MyBatisTestConfiguration` **不会**导入 `MyBatisDataBaseTestConfiguration` ——
> 不显式导入则不会建表。

## 使用示例（摘自本模块与 spring 模块测试）

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

spring 模块测试使用的标准搭配 —— 数据源 + 表结构生命周期：

```java
@SpringJUnitConfig(classes = {
        MyBatisDataBaseTestConfiguration.class,
        MyBatisDataSourceTestConfiguration.class,
        MyBatisDataBaseTestConfigurationTest.class
})
class MyBatisDataBaseTestConfigurationTest { ... }
```

```java
// 与 @EnableMyBatis（spring 模块）搭配
@EnableMyBatis
@Import(value = {
        MyBatisDataSourceTestConfiguration.class,
        MyBatisDataBaseTestConfiguration.class
})
static class DefaultConfig { }
```

## 另见

- [spring 模块](spring.md) · [test 模块](test.md)
- [开发者文档：spring-test](../../../developer-guide/current/zh/spring-test.md)
