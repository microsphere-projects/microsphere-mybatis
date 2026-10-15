# microsphere-mybatis-spring-test —— 开发者文档（`current`）

[English](../spring-test.md) · [← 索引](README.md) · [用户文档](../../../user-guide/current/zh/spring-test.md)

三个 `@Configuration(proxyBeanMethods = false)` 类，把纯测试夹具重新包装成 Spring bean。
刻意做得很薄：没有条件注解、没有 profile、没有自定义命名空间。

## 架构角色

```mermaid
graph LR
    MTU[MyBatisTestUtils\n（test 模块）] --> DSTC[MyBatisDataSourceTestConfiguration]
    DSTC -->|@Import| MTC[MyBatisTestConfiguration]
    DSTC -->|共享 DataSource| DBTC[MyBatisDataBaseTestConfiguration]
    DSTC --> SPRING["@EnableMyBatis\n（spring 模块测试）"]
    DBTC --> SPRING
```

## 源码分析

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

两处刻意为之的细节：

1. `Configuration` bean 的类型写全限定名 —— 在 Spring `@Configuration` 类里，
   简单名会与注解自身的 `Configuration` import 冲突。
2. 暴露的 `DataSource` 是 MyBatis 的 `PooledDataSource`，**归夹具 `Environment` 所有**，
   不是 Spring 创建的连接池。因此才有 `destroyMethod = "forceCloseAll"` —— 该方法只有
   `PooledDataSource` 才有（`DataSource` 没有 close 约定），上下文关闭时它会把已借出的
   连接强制归还。`@Primary` 让 `@EventListener` 与 `SqlSessionFactoryBean` 能无歧义地注入它。

### `MyBatisDataBaseTestConfiguration`

没有任何 bean —— 纯粹的生命周期观察者：

```java
@EventListener(ContextRefreshedEvent.class) → runCreateDatabaseScript(context.getBean(DataSource.class))
@EventListener(ContextClosedEvent.class)   → runDestroyDatabaseScript(...)
```

与 JUnit 扩展路径的作用域差异：表结构的生命周期是**每个应用上下文**一次，而非每个测试
方法一次。同一上下文中的所有 `@SpringJUnitConfig` 测试共享数据表；需要干净状态的测试要
用 `@DirtiesContext` 或手工脚本。该类**不会**被 `MyBatisTestConfiguration` 导入 ——
建表是可选的（参见 `EnableMyBatisTest.DefaultConfig` 同时导入两者）。

### `MyBatisTestConfiguration`

手工包装 `org.mybatis.spring.SqlSessionFactoryBean`：

```java
SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
bean.setConfiguration(configuration);
bean.setDataSource(dataSource);
return bean.getObject();
```

直接调用 `getObject()`（该配置类本身不是 `FactoryBean`），因此在 bean 创建时就急切地
构建了工厂。三个 mapper bean 来自包私有的 `getMapper` 辅助方法：在 try-with-resources
中打开会话、会话关闭后返回代理 —— 这是安全的，因为 MyBatis 的 mapper 代理会通过工厂
为每条语句重新打开会话。

## 设计模式

| 模式 | 位置 |
|---|---|
| 外观 | 夹具类把 MyBatis/H2 细节藏在标准 bean 类型之后 |
| 观察者 | 用 `@EventListener` 监听上下文事件驱动数据库生命周期 |
| 组合优于继承 | `MyBatisTestConfiguration` 通过 `@Import` 组合数据源配置而非继承 |

## 另见

- [test 源码分析](test.md) —— 夹具的来源
- [spring 源码分析](spring.md) —— 标准消费方配对
