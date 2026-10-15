# microsphere-mybatis-spring-cloud —— 用户文档

[English](../spring-cloud.md) · [开发者文档](../../../developer-guide/current/zh/spring-cloud.md) · [← 索引](README.md)

Spring Cloud 集成模块：通过 Spring Cloud / Actuator 的 `HasFeatures` 机制把 MyBatis
技术栈暴露为**特性信息**。本模块不声明任何功能性 Bean —— Executor 扩展的自动配置仍由
[spring-boot](spring-boot.md) 模块负责。

## 版本信息

| 项目 | 值 |
|---|---|
| 构件 | `io.github.microsphere-projects:microsphere-mybatis-spring-cloud` |
| 文档对应版本 | `0.2.16`（`release` 分支，0.2.x 线） |
| Java 基线 | 17 |
| 依赖 | `microsphere-mybatis-spring-boot`；optional：`spring-cloud-commons`、`spring-boot-starter-actuator`、`microsphere-spring-cloud-commons` `0.2.26` |
| 注册方式 | `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`；以 MyBatis 可用性为条件；排在官方 `MybatisAutoConfiguration` 之后 |

## 安装

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-mybatis-spring-cloud</artifactId>
</dependency>
```

启用 actuator 端点（与集成测试一致）：

```properties
spring.cloud.features.enabled=true
management.endpoints.web.exposure.include=*
```

## 提供内容

`MyBatisCloudAutoConfiguration` 激活随包发布的目录文件
`META-INF/config/default/features.yaml`，生成以下 `HasFeatures` Bean：

| Bean 名 | 抽象特性（扩展点） |
|---|---|
| `mybatis.features` | 19 个 MyBatis SPI 类型：`Executor`、`Interceptor`、`TypeHandler`、`LanguageDriver`、`DatabaseIdProvider`、`DataSourceFactory`、`SqlSessionFactory`、`TransactionFactory`、`Configuration`、`Cache`、`KeyGenerator`、`ProxyFactory`、`ParameterHandler`、`ResultContext`、`ResultSetHandler`、`StatementHandler`、`ResultHandler`、`SqlSource`、`ReflectorFactory` |
| `mybatis-spring.features` | `SqlSessionTemplate`、`MapperFactoryBean`、`MapperScannerConfigurer`、`MapperScannerBeanDefinitionParser`、`SqlSessionFactoryBean`、`SpringManagedTransactionFactory` |
| `mybatis-spring-boot-autoconfigure.features` | `MybatisProperties`、`ConfigurationCustomizer`、`SqlSessionFactoryBeanCustomizer` |
| `microsphere-mybatis-core.features` | `io.microsphere.mybatis.executor.ExecutorFilter`、`ExecutorInterceptor` |

外加具名特性 `microsphere-mybatis-spring-boot → ConditionalOnMyBatisAvailable`。

## 使用示例（摘自 `MyBatisCloudAutoConfigurationIntegrationTest`）

```java
@SpringBootTest(classes = {
        LoggingExecutorFilter.class,
        LoggingExecutorInterceptor.class,
        MyBatisCloudAutoConfigurationIntegrationTest.class
},
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {"spring.cloud.features.enabled=true",
                      "management.endpoints.web.exposure.include=*"})
@EnableAutoConfiguration
class MyBatisCloudAutoConfigurationIntegrationTest {

    @Autowired
    private Map<String, HasFeatures> hasFeaturesMap;

    @Autowired
    private FeaturesEndpoint featuresEndpoint;

    @Test
    void test() {
        assertNotNull(hasFeaturesMap.get("mybatis.features"));
        assertNotNull(hasFeaturesMap.get("mybatis-spring.features"));
        assertNotNull(hasFeaturesMap.get("mybatis-spring-boot-autoconfigure.features"));
        assertNotNull(hasFeaturesMap.get("microsphere-mybatis-core.features"));
        assertNotNull(featuresEndpoint.features());
    }
}
```

通过 HTTP 访问标准的 Spring Cloud/Actuator features 端点，即可获知应用 classpath 上存在
哪些 MyBatis 扩展点。

## 禁用

与 boot 模块相同的开关：`microsphere.mybatis.enabled=false`（缺少
`spring-cloud-commons` / actuator 时本模块自动失效）。

## 另见

- [spring-boot 模块](spring-boot.md)
- [开发者文档：spring-cloud](../../../developer-guide/current/zh/spring-cloud.md)
