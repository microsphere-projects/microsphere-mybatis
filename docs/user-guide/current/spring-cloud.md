# microsphere-mybatis-spring-cloud — User Guide

[中文](zh/spring-cloud.md) · [Developer Guide](../../developer-guide/current/spring-cloud.md) · [← Index](../README.md)

Spring Cloud integration that exposes the MyBatis technology stack as **feature information**
through the Spring Cloud / Actuator `HasFeatures` mechanism. It declares no functional beans —
auto-configuration of the Executor extension stays in
[spring-boot](spring-boot.md).

## Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-mybatis-spring-cloud` |
| Documented version | `0.2.16` (branch `release`, 0.2.x line) |
| Java baseline | 17 |
| Requires | `microsphere-mybatis-spring-boot`; optional: `spring-cloud-commons`, `spring-boot-starter-actuator`, `microsphere-spring-cloud-commons` `0.2.26` |
| Registration | `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`; conditional on MyBatis availability; ordered after the official `MybatisAutoConfiguration` |

## Installation

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-mybatis-spring-cloud</artifactId>
</dependency>
```

Enable the actuator endpoint (as the integration test does):

```properties
spring.cloud.features.enabled=true
management.endpoints.web.exposure.include=*
```

## What You Get

`MyBatisCloudAutoConfiguration` activates the shipped catalogue
`META-INF/config/default/features.yaml`, which materialises these `HasFeatures` beans:

| Bean name | Abstract features (extension points) |
|---|---|
| `mybatis.features` | 19 MyBatis SPI types: `Executor`, `Interceptor`, `TypeHandler`, `LanguageDriver`, `DatabaseIdProvider`, `DataSourceFactory`, `SqlSessionFactory`, `TransactionFactory`, `Configuration`, `Cache`, `KeyGenerator`, `ProxyFactory`, `ParameterHandler`, `ResultContext`, `ResultSetHandler`, `StatementHandler`, `ResultHandler`, `SqlSource`, `ReflectorFactory` |
| `mybatis-spring.features` | `SqlSessionTemplate`, `MapperFactoryBean`, `MapperScannerConfigurer`, `MapperScannerBeanDefinitionParser`, `SqlSessionFactoryBean`, `SpringManagedTransactionFactory` |
| `mybatis-spring-boot-autoconfigure.features` | `MybatisProperties`, `ConfigurationCustomizer`, `SqlSessionFactoryBeanCustomizer` |
| `microsphere-mybatis-core.features` | `io.microsphere.mybatis.executor.ExecutorFilter`, `ExecutorInterceptor` |

plus a named feature `microsphere-mybatis-spring-boot →
ConditionalOnMyBatisAvailable`.

## Usage Example (from `MyBatisCloudAutoConfigurationIntegrationTest`)

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

Via HTTP, the standard Spring Cloud Contract/Actuator features endpoint then reports which
MyBatis extension points are present on the application classpath.

## Disable

The same switch as the boot module applies: `microsphere.mybatis.enabled=false` (and the module
is inert without `spring-cloud-commons` / actuator on the classpath).

## See Also

- [spring-boot module](spring-boot.md)
- [Developer Guide: spring-cloud](../../developer-guide/current/spring-cloud.md)
