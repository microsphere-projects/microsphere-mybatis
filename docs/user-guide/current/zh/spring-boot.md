# microsphere-mybatis-spring-boot —— 用户文档

[English](../spring-boot.md) · [开发者文档](../../../developer-guide/current/zh/spring-boot.md) · [← 索引](README.md)

Spring Boot 自动配置模块：自动套用 `@EnableMyBatisExtension` 行为 ——
应用中任意 `ExecutorFilter` / `ExecutorInterceptor` Bean（或 SPI 声明的实现）
都会被接入 MyBatis `SqlSessionFactory`，无需修改任何 MyBatis 配置。

## 版本信息

| 项目 | 值 |
|---|---|
| 构件 | `io.github.microsphere-projects:microsphere-mybatis-spring-boot` |
| 文档对应版本 | `0.2.16`（`release` 分支，0.2.x 线） |
| Java 基线 | 17 |
| 依赖 | `microsphere-mybatis-spring`；optional：`mybatis-spring-boot-starter` `3.0.5`、Spring Boot 3.x |
| 注册方式 | `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（Spring Boot 2.7+ 机制，**不再是** `spring.factories`；0.1.x 线使用 `spring.factories`，见 [1.x 文档](../../1.x/zh/spring-boot.md)） |
| 顺序 | `@AutoConfigureAfter(name = "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration")` —— 官方 starter 负责构建 `sqlSessionFactory`/`sqlSessionTemplate`，本模块只做扩展 |

## 安装

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-mybatis-spring-boot</artifactId>
</dependency>
<!-- 官方 starter 仍需自行引入 -->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
</dependency>
```

## 配置属性

| 属性 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `microsphere.mybatis.enabled` | `boolean` | `true` | 设为 `false` 可完全关闭 `MyBatisAutoConfiguration` |

自动配置条件（`@ConditionalOnMyBatisAvailable`）：

1. `@ConditionalOnMyBatisEnabled` —— 上述属性检查（`matchIfMissing = true`）。
2. `@ConditionalOnClass` —— classpath 上存在 `org.apache.ibatis.session.SqlSession` 与 `SqlSessionFactory`。

## 参与方式

声明 `ExecutorFilter` / `ExecutorInterceptor` Bean 即可 —— 这就是全部 API：

```java
@SpringBootApplication
public class MyApplication {

    @Bean
    public ExecutorFilter metricsExecutorFilter() {
        return new MetricsExecutorFilter();
    }

    @Bean
    public ExecutorInterceptor auditExecutorInterceptor() {
        return new AuditExecutorInterceptor();
    }
}
```

需要了解的注册 Bean 名（来自 `MyBatisExtensionBeanDefinitionRegistrar`）：
`interceptingExecutorInterceptor`（MyBatis 插件），以及每个被发现的过滤器/拦截器各一个 Bean
（如 `loggingExecutorFilter`）。

## 测试用例示例

存在性测试（`MyBatisAutoConfigurationIntegrationTest`）：

```java
@SpringBootTest(classes = {
        LoggingExecutorFilter.class,
        LoggingExecutorInterceptor.class,
        MyBatisAutoConfigurationIntegrationTest.class
})
@EnableAutoConfiguration
class MyBatisAutoConfigurationIntegrationTest {

    @Autowired @Qualifier(SQL_SESSION_FACTORY_BEAN_NAME)
    private SqlSessionFactory sqlSessionFactory;

    @Autowired @Qualifier(SQL_SESSION_TEMPLATE_BEAN_NAME)
    private SqlSessionTemplate sqlSessionTemplate;

    @Autowired @Qualifier(INTERCEPTING_EXECUTOR_INTERCEPTOR_BEAN_NAME)
    private InterceptingExecutorInterceptor interceptor;   // 证明插件已注册
}
```

关闭测试（`ConditionalOnMyBatisEnabledTest`）：

```java
@SpringBootTest(classes = EnabledConfig.class, properties = "microsphere.mybatis.enabled = false")
class ConditionalOnMyBatisEnabledTest { /* 断言配置 Bean 不存在 */ }
```

自动配置契约测试（`MyBatisAutoConfigurationTest`）继承
`AutoConfigurationTest<MyBatisAutoConfiguration>`，验证三种场景：可被自动配置；
全局禁用属性可移除；缺少 MyBatis 类时移除。

## 另见

- [core 模块](core.md) —— 过滤器/拦截器能力
- [spring 模块](spring.md) —— 非 Boot 环境的等价物（`@EnableMyBatisExtension`）
- [开发者文档：spring-boot](../../../developer-guide/current/zh/spring-boot.md)
