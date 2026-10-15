# microsphere-mybatis-utils —— 用户文档

[English](../utils.md) · [开发者文档](../../../developer-guide/current/zh/utils.md) · [← 索引](README.md)

提供在**不依赖任何容器**的情况下引导并使用 MyBatis 的静态工具方法：加载属性文件与 XML 配置、
构建 `Configuration` / `SqlSessionFactory`、创建 `Connection`、`Transaction`、`Executor`，以及执行 SQL 脚本。

## 版本信息

| 项目 | 值 |
|---|---|
| 构件 | `io.github.microsphere-projects:microsphere-mybatis-utils` |
| 文档对应版本 | `0.2.16`（`release` 分支，0.2.x 线） |
| Java 基线 | 17 |
| 依赖 | MyBatis `3.5.19`（编译期 `optional`，需自行引入） |
| API 稳定性 | 全部类型 `@since 1.0.0`；0.2.x 线仅含 javadoc/依赖更新 |

> 已发布的 BOM（`microsphere-mybatis-dependencies`）**未管理**本构件版本。
> 建议通过 `microsphere-mybatis-core` 传递引入，或自行固定版本。

## 安装

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-mybatis-utils</artifactId>
    <version>0.2.16</version>
</dependency>
```

## API 说明 —— `io.microsphere.mybatis.util.MyBatisUtils`

`MyBatisUtils` 为抽象工具类（私有构造器、全静态方法）。

### 引导类

| 方法 | 说明 |
|---|---|
| `static Properties loadProperties(String propertiesResource)` | 加载 classpath 上的 `.properties` 资源（如 `META-INF/mybatis/mybatis.properties`） |
| `static Configuration getConfiguration(String configResource)` | 解析 XML 配置（不指定 environment/属性） |
| `static Configuration getConfiguration(String configResource, String environment)` | 解析并选择具名 `<environment>` |
| `static Configuration getConfiguration(String configResource, String environment, Properties properties)` | 解析并做 `${...}` 变量替换（包装 MyBatis `XMLConfigBuilder`） |
| `static SqlSessionFactory buildSqlSessionFactory(Configuration)` | 返回 `DefaultSqlSessionFactory` |
| `static SqlSession openSession(Configuration)` | 便捷方法：工厂 + `openSession()`，调用方负责关闭 |

### 组件访问

| 方法 | 说明 |
|---|---|
| `static Environment getEnvironment(Configuration)` | 读取 `configuration.getEnvironment()` |
| `static DataSource getDataSource(Configuration \| Environment)` | environment 中的 `DataSource` |
| `static Connection getConnection(Configuration)` | 打开会话并返回连接（**不会**自动关闭） |
| `static Transaction newTransaction(Configuration, Connection)` | 通过 environment 的 `TransactionFactory` 创建事务 |
| `static Executor newExecutor(Configuration, Connection)` | 即 `configuration.newExecutor(transaction)`，遵循配置的 executor 类型与插件 |

### 脚本执行

| 方法 | 说明 |
|---|---|
| `static void runScript(DataSource, String scriptResource)` | 以 `ScriptRunner` 执行 DDL/DML（auto-commit 开、遇错不停、日志静默） |
| `static void runScript(ScriptRunner, String resource)` | 使用预配置 runner 执行脚本 |

### 回调模板（try-with-resources 语义）

| 方法 | 说明 |
|---|---|
| `static void doInSqlSession(Configuration, ThrowableConsumer<SqlSession>)` | 自动开关 `SqlSession` |
| `static void doInConnection(Configuration, ThrowableConsumer<Connection>)` | 同时关闭连接 |
| `static void doInExecutor(Configuration, ThrowableConsumer<Executor>)` | 创建带独立事务的 `Executor`，结束后 `close(false)` |

`ThrowableConsumer` lambda 可抛出受检异常，方法以 `throws Throwable` 传播。

## 代码示例

与类 javadoc 及 `microsphere-mybatis-test` 固件（H2 内存库）一致：

```java
// 1. 加载属性并解析 XML 配置
Properties props = MyBatisUtils.loadProperties("META-INF/mybatis/mybatis.properties");
Configuration configuration =
    MyBatisUtils.getConfiguration("META-INF/mybatis/config.xml", "testing", props);

// 2. 初始化表结构
MyBatisUtils.runScript(MyBatisUtils.getDataSource(configuration), "META-INF/sql/init-db.sql");

// 3. 直接操作 Executor（可访问事务/语句）
MyBatisUtils.doInExecutor(configuration, executor -> {
    Transaction transaction = executor.getTransaction();
    executor.update(configuration.getMappedStatement(
        "io.microsphere.mybatis.test.mapper.UserMapper.saveUser"), new User(1, "Mercy"));
    transaction.commit();
});

// 4. 或通过 SqlSession 与 Mapper
MyBatisUtils.doInSqlSession(configuration, sqlSession -> {
    UserMapper mapper = sqlSession.getMapper(UserMapper.class);
    mapper.saveUser(new User(2, "John Smith"));
    sqlSession.commit();
});

// 5. 清理
MyBatisUtils.runScript(MyBatisUtils.getDataSource(configuration), "META-INF/sql/destroy-db.sql");
```

## 另见

- [microsphere-mybatis-core](core.md) —— 基于本工具类的 Executor 过滤器链
- [microsphere-mybatis-test](test.md) —— 提供示例中使用的现成固件资源
