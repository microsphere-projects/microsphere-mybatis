# microsphere-mybatis-test —— 用户文档（1.x / 0.1.x）

[English](../test.md) · [开发者文档](../../../developer-guide/1.x/zh/test.md) · [← 1.x 索引](README.md)

JUnit 5 扩展（`@MyBatisTest` / `@MyBatisRuntime`）、抽象测试基类体系与 H2 固件资源在 0.1.x
与 `current` 中相同，完整说明见 [current/zh/test.md](../../current/zh/test.md)。1.x 特有信息：

## 版本信息

| 项目 | 值 |
|---|---|
| 构件 | `io.github.microsphere-projects:microsphere-mybatis-test` |
| 文档对应版本 | `0.1.16`（`release-1.x` 分支，0.1.x 线） |
| Java 基线 | **8** |
| 依赖 | `microsphere-mybatis-utils`；optional：MyBatis `3.5.19`、JUnit Jupiter 5.x、H2、logback |

## 1.x 说明

- **仅支持 JUnit 5（Jupiter）**。抽象基类使用 `org.junit.jupiter.api` 的
  `@BeforeEach`/`@AfterEach`/`@Test`，不提供 JUnit 4 支持。
- 0.1.x 的 `MyBatisTestExtension` 中为传统 `instanceof AutoCloseable` 强转，`current` 使用
  模式匹配 —— 行为一致。
- 解析器 SPI 文件仍为
  `META-INF/services/io.microsphere.mybatis.test.junit.jupiter.resolver.ComponentResolver`，
  同样的 10 个解析器、同样的匹配顺序与 static/实例支持矩阵。
- 消费方引入（test scope）：

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-mybatis-test</artifactId>
    <version>0.1.16</version>
    <scope>test</scope>
</dependency>
```

## 用法回顾

```java
@MyBatisTest
class MyDaoTest {

    @MyBatisRuntime
    private UserMapper userMapper;   // 实例字段注入

    @Test
    void test(@MyBatisRuntime SqlSession sqlSession) {
        assertNotNull(sqlSession);
    }
}
```

## 另见

- [完整参考（current）](../../current/zh/test.md) —— 属性表、解析器列表、固件清单
