# microsphere-mybatis-spring —— 用户文档

[English](../spring.md) · [开发者文档](../../../developer-guide/current/zh/spring.md) · [← 索引](README.md)

面向**原生 Spring**（无需 Spring Boot）的注解驱动 MyBatis 支持：

- `@EnableMyBatis` —— 依据注解属性注册 `sqlSessionFactory` 与 `sqlSessionTemplate`。
- `@MyBatisConfiguration` —— 逐属性注册 `org.apache.ibatis.session.Configuration` Bean。
- `@EnableMyBatisExtension` —— 自动把[核心模块](core.md)的 `ExecutorFilter` /
  `ExecutorInterceptor` 链接入 `SqlSessionFactory`。

## 版本信息

| 项目 | 值 |
|---|---|
| 构件 | `io.github.microsphere-projects:microsphere-mybatis-spring` |
| 文档对应版本 | `0.2.16`（`release` 分支，0.2.x 线） |
| Java 基线 | 17 |
| 依赖 | `microsphere-mybatis-core`；optional：Spring 6.x、MyBatis-Spring `4.1.0`、`microsphere-spring-context` `0.2.38` |
| Spring 兼容性 | 0.2.x 线面向 Spring Framework 6.x（jakarta 命名空间）。Spring 5.x（javax）请使用 [1.x 线](../../1.x/zh/spring.md) |

## 安装

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-mybatis-spring</artifactId>
</dependency>
```

## API 说明 —— `@EnableMyBatis`

标注在任意 `@Configuration` 类上（元 `@Import` `MyBatisBeanDefinitionRegistrar`）。

| 属性 | 类型 | 默认值 | 含义 |
|---|---|---|---|
| `dataSource` | `String` | `"*"` | `DataSource` Bean 名；`*` 取 primary；空串为不设置 |
| `configLocation` | `String` | `""` | MyBatis XML 配置资源；为空则复用 `configuration` Bean |
| `checkConfigLocation` | `boolean` | `false` | 启动时断言资源存在 |
| `mapperLocations` | `String[]` | `{}` | Mapper XML 资源（支持占位符） |
| `typeAliasesPackage` | `String[]` | `{}` | 类型别名扫描包 |
| `typeAliasesSuperType` | `Class<?>` | `Object.class` | 别名过滤父类型 |
| `typeHandlersPackage` | `String[]` | `{}` | TypeHandler 扫描包 |
| `executorType` | `ExecutorType` | `SIMPLE` | 应用于 `SqlSessionTemplate` |
| `vfs` | `Class<? extends VFS>` | `VFS.class` | 自定义 VFS |
| `defaultScriptingLanguageDriver` | `Class<? extends LanguageDriver>` | `LanguageDriver.class` | 默认语言驱动 |
| `configurationProperties` | `String[]` | `{}` | `"key = value"` 形式的条目；格式非法将抛 `IllegalArgumentException` |
| `configurationPropertiesImportPropertySources` | `boolean` | `false` | 先导入全部 Spring `PropertySource`（注解条目仍可覆盖） |
| `objectWrapperFactory` | `String` | `""` | Bean 引用（`*` 为 primary） |
| `databaseIdProvider` | `String` | `"*"` | Bean 引用 |
| `cache` | `String` | `""` | 二级缓存 `Cache` 实现 Bean 引用 |
| `plugins` | `String[]` | `{"*"}` | 挂载的 `Interceptor` Bean（`*` 全部；`{}`/空串为不挂载） |
| `typeHandlers` | `String[]` | `{"*"}` | `TypeHandler` Bean |
| `scriptingLanguageDrivers` | `String[]` | `{"*"}` | `LanguageDriver` Bean |

注册的 Bean（仅当不存在时——自定义优先）：`sqlSessionFactory`
（`SqlSessionFactoryBean`）与 `sqlSessionTemplate`（`SqlSessionTemplate`）。

## API 说明 —— `@MyBatisConfiguration`

注册名为 `configuration` 的 Bean，属性与 `org.apache.ibatis.session.Configuration`
的 setter 一一对应。不能与非空的 `@EnableMyBatis.configLocation` 同时使用（XML 优先）。

默认值与 MyBatis 一致：`cacheEnabled=true`、`lazyLoadingEnabled=false`、
`aggressiveLazyLoading=false`、`multipleResultSetsEnabled=true`（已废弃）、
`useColumnLabel=true`、`useGeneratedKeys=false`、`autoMappingBehavior=PARTIAL`、
`autoMappingUnknownColumnBehavior=NONE`、`defaultExecutorType=SIMPLE`、
`defaultStatementTimeout=-1`（未设置）、`defaultFetchSize=-1`（未设置）、
`defaultResultSetType=DEFAULT`、`safeRowBoundsEnabled=false`、`safeResultHandlerEnabled=true`、
`mapUnderscoreToCamelCase=false`、`localCacheScope=SESSION`、`jdbcTypeForNull=OTHER`、
`lazyLoadTriggerMethods={"equals","clone","hashCode","toString"}`、
`defaultScriptingLanguage=XMLLanguageDriver`、`defaultEnumTypeHandler=EnumTypeHandler`、
`callSettersOnNulls=false`、`returnInstanceForEmptyRow=false`、`useActualParamName=true`、
`shrinkWhitespacesInSql=false`、`nullableOnForEach=false`、
`argNameBasedConstructorAutoMapping=false`，另有 `variables : String[]`、`logPrefix`、`logImpl`、
`proxyFactory`、`vfsImpl`、`configurationFactory`、`defaultSqlProviderType`、`databaseId`。
与默认值相同的属性不会写入 BeanDefinition。

## API 说明 —— `@EnableMyBatisExtension`

| 属性 | 类型 | 默认值 | 含义 |
|---|---|---|---|
| `interceptExecutor` | `boolean` | `true` | 整个扩展的总开关 |
| `sources` | `BeanSource[]` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | `ExecutorFilter` / `ExecutorInterceptor` 实现的发现来源 |

行为：

1. 从所选来源发现 `ExecutorFilter` 与 `ExecutorInterceptor` 类型
   （`BEAN_FACTORY` = 已有 Bean 定义；`SPRING_FACTORIES` = `META-INF/spring.factories`；
   `JAVA_SERVICE_PROVIDER` = `META-INF/services`），并把缺失者注册为 Bean。
2. 若至少存在一个过滤器/拦截器，注册 Bean **`interceptingExecutorInterceptor`**
   （`InterceptingExecutorInterceptor`，`dependsOn` 全部过滤器/拦截器 Bean）。
3. 注册 `SqlSessionFactoryBeanPostProcessor`，在初始化前把该拦截器追加进每个
   `SqlSessionFactoryBean` 的 `plugins` —— 无需修改 XML 或 `@MapperScan`。

## 代码示例（摘自 `EnableMyBatisTest` / `EnableMyBatisExtensionTest`）

```java
// 最小配置 —— 复用 microsphere-mybatis-test 固件
@EnableMyBatis
@Import(value = {
        MyBatisDataSourceTestConfiguration.class,   // 提供 MyBatis Configuration + DataSource
        MyBatisDataBaseTestConfiguration.class      // 在容器事件时执行 init-db.sql / destroy-db.sql
})
static class DefaultConfig {
}
```

```java
// 完整显式配置
@EnableMyBatis(dataSource = "dataSource",
               configLocation = DEFAULT_CONFIG_RESOURCE_NAME, checkConfigLocation = true,
               plugins = {}, typeHandlers = {""}, scriptingLanguageDrivers = {"", " "})
static class DataSourceConfig {
}

@EnableMyBatis(mapperLocations = {"${user-mapper-resource}", "${child-mapper-resource}", "${father-mapper-resource}"},
               typeAliasesPackage = "io.microsphere.mybatis.test.entity",
               typeAliasesSuperType = Serializable.class,
               executorType = ExecutorType.REUSE)
@MyBatisConfiguration                                // 无 configLocation -> 由注解构建 Configuration
@PropertySource("classpath:META-INF/mybatis/mybatis.properties")
static class MapperConfig2 {
}
```

```java
// 扩展：以 @Import 的普通 Bean 声明过滤器/拦截器
@EnableMyBatisExtension(sources = BeanSource.BEAN_FACTORY)
@Import(value = {LoggingExecutorFilter.class, LoggingExecutorInterceptor.class})
static class BeanFactorySourceConfig {
}

// ...或由 META-INF/spring.factories / META-INF/services 发现
@EnableMyBatisExtension(sources = BeanSource.SPRING_FACTORIES)
static class SpringFactoriesSourceConfig {
}
```

获取产物 Bean（注意 `FactoryBean` 前缀）：

```java
SqlSessionFactoryBean factoryBean =
    (SqlSessionFactoryBean) beanFactory.getBean(BeanFactory.FACTORY_BEAN_PREFIX + "sqlSessionFactory");
SqlSessionTemplate template = beanFactory.getBean(SqlSessionTemplate.class);
UserMapper mapper = template.getMapper(UserMapper.class);
```

## 另见

- [spring-boot 模块](spring-boot.md) —— 由自动配置完成同样的扩展
- [spring-test 模块](spring-test.md) —— 示例所使用的固件配置
- [开发者文档：spring](../../../developer-guide/current/zh/spring.md) —— 注册器内部机制与流程图
