# microsphere-mybatis-spring-test —— 用户文档（1.x / 0.1.x）

[English](../spring-test.md) · [开发者文档](../../../developer-guide/1.x/zh/spring-test.md) · [← 1.x 索引](README.md)

三个固件配置（`MyBatisDataSourceTestConfiguration`、`MyBatisDataBaseTestConfiguration`、
`MyBatisTestConfiguration`）与 0.2.x 线完全相同，Bean 表与示例见
[current/zh/spring-test.md](../../current/zh/spring-test.md)。

## 版本信息

| 项目 | 值 |
|---|---|
| 构件 | `io.github.microsphere-projects:microsphere-mybatis-spring-test` |
| 文档对应版本 | `0.1.16`（`release-1.x` 分支，0.1.x 线） |
| Java 基线 | **8** |
| 依赖 | `microsphere-mybatis-test` 0.1.16；optional：MyBatis-Spring **2.1.2**、Spring **5.x**、H2、JUnit Jupiter |

## 1.x 说明

- `MyBatisTestConfiguration` 使用来自 **mybatis-spring 2.1.2** 的
  `org.mybatis.spring.SqlSessionFactoryBean` 构建 `SqlSessionFactory`（类名相同，代际较旧）。
- `MyBatisDataBaseTestConfiguration` 的 `@EventListener` 数据库生命周期在 Spring 5 上工作如常。
- 配对规则同样适用：需要执行 `init-db.sql` 时必须显式导入
  `MyBatisDataBaseTestConfiguration`。

```java
@EnableMyBatis
@Import(value = {
        MyBatisDataSourceTestConfiguration.class,
        MyBatisDataBaseTestConfiguration.class
})
static class DefaultConfig {
}
```

## 另见

- [current/zh/spring-test.md](../../current/zh/spring-test.md) —— 完整 Bean 参考
