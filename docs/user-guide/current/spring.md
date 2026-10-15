# microsphere-mybatis-spring — User Guide

[中文](zh/spring.md) · [Developer Guide](../../developer-guide/current/spring.md) · [← Index](../README.md)

Annotation-driven MyBatis support for **plain Spring** (no Spring Boot required):

- `@EnableMyBatis` — registers `sqlSessionFactory` + `sqlSessionTemplate` from annotation attributes.
- `@MyBatisConfiguration` — registers a `org.apache.ibatis.session.Configuration` bean, attribute
 by attribute.
- `@EnableMyBatisExtension` — plugs the [core module's](core.md) `ExecutorFilter` /
  `ExecutorInterceptor` chain into the `SqlSessionFactory` automatically.

## Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-mybatis-spring` |
| Documented version | `0.2.16` (branch `release`, 0.2.x line) |
| Java baseline | 17 |
| Requires | `microsphere-mybatis-core`; optional: Spring 6.x (`spring-context`/`spring-jdbc`), MyBatis-Spring `4.1.0`, `microsphere-spring-context` `0.2.38` |
| Spring compatibility | This 0.2.x line targets Spring Framework 6.x / jakarta namespace. For Spring 5.x (javax) use the [1.x line](../1.x/spring.md) |

## Installation

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-mybatis-spring</artifactId>
</dependency>
```

## API Reference — `@EnableMyBatis`

Placed on any `@Configuration` class (meta-`@Import`s `MyBatisBeanDefinitionRegistrar`).

| Attribute | Type | Default | Meaning |
|---|---|---|---|
| `dataSource` | `String` | `"*"` | Bean name of the `DataSource`; `*` = primary bean; blank = none |
| `configLocation` | `String` | `""` | MyBatis XML config resource; blank ⇒ reuse the `configuration` bean |
| `checkConfigLocation` | `boolean` | `false` | Assert the resource exists at startup |
| `mapperLocations` | `String[]` | `{}` | Mapper XML resources (placeholders supported) |
| `typeAliasesPackage` | `String[]` | `{}` | Packages scanned for type aliases |
| `typeAliasesSuperType` | `Class<?>` | `Object.class` | Alias filter super-type |
| `typeHandlersPackage` | `String[]` | `{}` | Type-handler packages |
| `executorType` | `ExecutorType` | `SIMPLE` | Applied to `SqlSessionTemplate` |
| `vfs` | `Class<? extends VFS>` | `VFS.class` | Custom VFS |
| `defaultScriptingLanguageDriver` | `Class<? extends LanguageDriver>` | `LanguageDriver.class` | Custom language driver |
| `configurationProperties` | `String[]` | `{}` | Entries of the form `"key = value"`; malformed entries fail with `IllegalArgumentException` |
| `configurationPropertiesImportPropertySources` | `boolean` | `false` | Seed properties from every Spring `PropertySource` first (annotation entries still win) |
| `objectWrapperFactory` | `String` | `""` | Bean reference (`*` = primary) |
| `databaseIdProvider` | `String` | `"*"` | Bean reference |
| `cache` | `String` | `""` | Bean reference for the second-level `Cache` implementation |
| `plugins` | `String[]` | `{"*"}` | `Interceptor` beans to attach (`*` = all, `{}`/blank = none) |
| `typeHandlers` | `String[]` | `{"*"}` | `TypeHandler` beans |
| `scriptingLanguageDrivers` | `String[]` | `{"*"}` | `LanguageDriver` beans |

Beans registered (only if absent — yours win): `sqlSessionFactory`
(`org.mybatis.spring.SqlSessionFactoryBean`) and `sqlSessionTemplate` (`SqlSessionTemplate`).

## API Reference — `@MyBatisConfiguration`

Registers a bean named `configuration` mapping 1:1 to `org.apache.ibatis.session.Configuration`
setters. Cannot be combined with a non-blank `@EnableMyBatis.configLocation` (the XML wins).

Defaults mirror MyBatis: `cacheEnabled=true`, `lazyLoadingEnabled=false`,
`aggressiveLazyLoading=false`, `multipleResultSetsEnabled=true` (deprecated),
`useColumnLabel=true`, `useGeneratedKeys=false`, `autoMappingBehavior=PARTIAL`,
`autoMappingUnknownColumnBehavior=NONE`, `defaultExecutorType=SIMPLE`,
`defaultStatementTimeout=-1` (unset), `defaultFetchSize=-1` (unset),
`defaultResultSetType=DEFAULT`, `safeRowBoundsEnabled=false`, `safeResultHandlerEnabled=true`,
`mapUnderscoreToCamelCase=false`, `localCacheScope=SESSION`,
`jdbcTypeForNull=OTHER`, `lazyLoadTriggerMethods={"equals","clone","hashCode","toString"}`,
`defaultScriptingLanguage=XMLLanguageDriver`, `defaultEnumTypeHandler=EnumTypeHandler`,
`callSettersOnNulls=false`, `returnInstanceForEmptyRow=false`, `useActualParamName=true`,
`shrinkWhitespacesInSql=false`, `nullableOnForEach=false`,
`argNameBasedConstructorAutoMapping=false`, plus `variables : String[]`, `logPrefix`,
`logImpl`, `proxyFactory`, `vfsImpl`, `configurationFactory`, `defaultSqlProviderType`,
`databaseId`. Values equal to a default are not written into the bean definition.

## API Reference — `@EnableMyBatisExtension`

| Attribute | Type | Default | Meaning |
|---|---|---|---|
| `interceptExecutor` | `boolean` | `true` | Master switch for the whole extension |
| `sources` | `BeanSource[]` | `{BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}` | Where to discover `ExecutorFilter` / `ExecutorInterceptor` implementations |

Behaviour:

1. Discovers `ExecutorFilter` and `ExecutorInterceptor` types from the selected sources
   (`BEAN_FACTORY` = existing bean definitions, `SPRING_FACTORIES` = `META-INF/spring.factories`,
   `JAVA_SERVICE_PROVIDER` = `META-INF/services`) and registers the missing ones as beans.
2. If at least one filter/interceptor exists, registers the bean
   **`interceptingExecutorInterceptor`** (`InterceptingExecutorInterceptor`, `dependsOn` every
   filter/interceptor bean).
3. Registers `SqlSessionFactoryBeanPostProcessor`, which adds the interceptor to every
   `SqlSessionFactoryBean`'s `plugins` before initialization — no XML or `@MapperScan` change needed.

## Code Examples (from `EnableMyBatisTest` / `EnableMyBatisExtensionTest`)

```java
// Minimal setup — reuses the microsphere-mybatis-test fixtures
@EnableMyBatis
@Import(value = {
        MyBatisDataSourceTestConfiguration.class,   // provides MyBatis Configuration + DataSource
        MyBatisDataBaseTestConfiguration.class      // runs init-db.sql / destroy-db.sql on context events
})
static class DefaultConfig {
}
```

```java
// Full explicit configuration
@EnableMyBatis(dataSource = "dataSource",
               configLocation = DEFAULT_CONFIG_RESOURCE_NAME, checkConfigLocation = true,
               plugins = {}, typeHandlers = {""}, scriptingLanguageDrivers = {"", " "})
static class DataSourceConfig {
}

@EnableMyBatis(mapperLocations = {"${user-mapper-resource}", "${child-mapper-resource}", "${father-mapper-resource}"},
               typeAliasesPackage = "io.microsphere.mybatis.test.entity",
               typeAliasesSuperType = Serializable.class,
               executorType = ExecutorType.REUSE)
@MyBatisConfiguration                                // no configLocation -> annotation-built Configuration
@PropertySource("classpath:META-INF/mybatis/mybatis.properties")
static class MapperConfig2 {
}

@EnableMyBatis(configurationProperties = {
        "jdbc.driver = org.h2.Driver",
        "jdbc.url = jdbc:h2:mem:test_mem"
})
static class ConfigurationPropetiesConfig {
}
```

```java
// Extension: filters/interceptors declared as plain @Import-ed beans
@EnableMyBatisExtension(sources = BeanSource.BEAN_FACTORY)
@Import(value = {LoggingExecutorFilter.class, LoggingExecutorInterceptor.class})
static class BeanFactorySourceConfig {
}

// ...or discovered from META-INF/spring.factories / META-INF/services
@EnableMyBatisExtension(sources = BeanSource.SPRING_FACTORIES)
static class SpringFactoriesSourceConfig {
}
```

Retrieving the produced beans (note the `FactoryBean` prefix):

```java
SqlSessionFactoryBean factoryBean =
    (SqlSessionFactoryBean) beanFactory.getBean(BeanFactory.FACTORY_BEAN_PREFIX + "sqlSessionFactory");
SqlSessionTemplate template = beanFactory.getBean(SqlSessionTemplate.class);
UserMapper mapper = template.getMapper(UserMapper.class);
```

## See Also

- [spring-boot module](spring-boot.md) — the same extension applied by auto-configuration
- [spring-test module](spring-test.md) — fixture configurations used by the examples above
- [Developer Guide: spring](../../developer-guide/current/spring.md) — registrar internals & diagrams
