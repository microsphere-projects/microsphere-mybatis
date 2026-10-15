# microsphere-mybatis-core —— 用户文档

[English](../core.md) · [开发者文档](../../../developer-guide/current/zh/core.md) · [← 索引](README.md)

核心模块强化了 MyBatis 针对 `Executor` 接口的插件体系：不再只有单个 JDK 代理 `Interceptor`，
而是引入有序的 **`ExecutorFilter` 链**（Servlet 风格，可观察结果与异常）与
**`ExecutorInterceptor`**（before/after 回调），并由一个标准 `Interceptor` 实现桥接进 MyBatis。

## 版本信息

| 项目 | 值 |
|---|---|
| 构件 | `io.github.microsphere-projects:microsphere-mybatis-core` |
| 文档对应版本 | `0.2.16`（`release` 分支，0.2.x 线） |
| Java 基线 | 17 |
| 依赖 | `microsphere-mybatis-utils`；MyBatis `3.5.19`（编译期 optional） |
| 兼容性 | MyBatis 3.5.x；在原生 MyBatis、mybatis-spring、Spring Boot 下行为一致 |
| API 稳定性 | 公开类型均 `@since 1.0.0`；0.2.x 线 API 稳定 |

## 安装

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-mybatis-core</artifactId>
    <!-- 版本由 microsphere-mybatis-dependencies BOM 管理 -->
</dependency>
```

## 核心概念

- **`ExecutorFilter`**：包裹一次 `Executor` 操作，通过 `ExecutorFilterChain` 显式续链；
  可以看到入参、返回值与 `SQLException`，可以短路（不调用 `chain.xxx(...)`）、装饰或替换结果。
- **`ExecutorInterceptor`**：声明式的 `beforeXxx` / `afterXxx` 回调。编写更简单，但无法否决调用；
  单个拦截器内的异常只会被记录（warn），不会传播。
- **`InterceptingExecutorInterceptor`**：唯一需要注册给 MyBatis 的对象
  （`configuration.addInterceptor(...)`），负责把目标 `Executor` 包装成携带全部过滤器的
  `InterceptingExecutor`。
- 两个扩展接口均继承 `io.microsphere.lang.Prioritized`，`getPriority()` 越小越先执行。

## 被拦截的操作

`Executor` 除 `flushStatements`、`isCached`、`clearLocalCache` 外的全部方法都经过过滤器链：
`update`、两个 `query` 重载、`queryCursor`、`commit`、`rollback`、`createCacheKey`、
`deferLoad`、`getTransaction`、`close`。

## API 说明

### `ExecutorFilter`

所有方法默认直接透传，只需覆盖关心的操作：

| 方法 | 返回 | 说明 |
|---|---|---|
| `int update(ms, parameter, chain)` | 影响行数 | 拦截 `Executor#update` |
| `<E> List<E> query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql, chain)` | 结果列表 | 六参缓存查询 |
| `<E> List<E> query(ms, parameter, rowBounds, resultHandler, chain)` | 结果列表 | 四参查询 |
| `<E> Cursor<E> queryCursor(ms, parameter, rowBounds, chain)` | 游标 | 流式查询 |
| `void commit(boolean required, chain)` / `void rollback(...)` | — | 事务边界 |
| `CacheKey createCacheKey(...)` | 缓存键 | 二级缓存键 |
| `void deferLoad(...)` | — | 懒加载嵌套查询 |
| `Transaction getTransaction(chain)` | 事务 | |
| `void close(boolean forceRollback, chain)` | — | 执行器关闭 |

### `ExecutorInterceptor`

成对回调（默认空实现，`failure` 非空表示调用抛异常）：

| Before | After |
|---|---|
| `beforeUpdate(context, ms, parameter)` | `afterUpdate(context, ms, parameter, result, failure)` |
| `beforeQuery(context, ms, parameter, rowBounds, resultHandler, cacheKey, boundSql)` | `afterQuery(…, result, failure)` |
| `beforeQueryCursor(...)` | `afterQueryCursor(…, result, failure)` |
| `beforeCommit(context, required)` / `beforeRollback(...)` | `afterCommit(…, failure)` / `afterRollback(…, failure)` |
| `beforeCreateCacheKey` / `beforeDeferLoad` / `beforeGetTransaction` | 对应 `afterXxx(…, failure)` |
| `beforeClose(context, forceRollback)` | `afterClose(context, forceRollback)` |

### `InterceptorContext<Executor>`

| 方法 | 说明 |
|---|---|
| `Executor getTarget()` | 被拦截（委托）执行器 |
| `Properties getProperties()` | 来自 `Interceptor#setProperties`，可为 `null` |
| `setStartTime(Long)` / `getStartTime()` | 计时支持（链式 setter） |
| `setAttribute` / `getAttribute(name[, default])` / `hasAttribute` / `removeAttribute(s)` | 单次调用的属性表，用于 before/after 间传数据 |
| `getAttributes()` | 只读视图 |

### `InterceptingExecutorInterceptor`

```java
new InterceptingExecutorInterceptor(ExecutorFilter[] filters, ExecutorInterceptor... interceptors)
```

- 过滤器与拦截器至少要提供一个，否则抛 `IllegalArgumentException`。
- 提供的 `ExecutorInterceptor` 会被包进一个 `InterceptorsExecutorFilterAdapter`，
  与过滤器合并后按 `Prioritized` 优先级排序。
- MyBatis 调用 `plugin(Object)` 时返回包装后的 `InterceptingExecutor`；若目标是
  `CachingExecutor`，会先反射取出其内部 delegate 再包装，保证过滤器观察到真实 SQL 执行，
  最后再套回新的 `CachingExecutor`。
- 允许多个 `InterceptingExecutorInterceptor` 叠加：过滤器与属性会自动合并。
- `intercept(Invocation)` 特意不使用——只记录 warn 并直接 `proceed()`。

### 内置实现

| 类 | 角色 |
|---|---|
| `LoggingExecutorFilter` | 每次操作以 `debug` 记录后透传 |
| `LoggingExecutorInterceptor` | 每个 before/after 回调以 `debug` 记录 |
| `InterceptingExecutor` | `Executor` 装饰器；每次调用 `buildChain()` 新建链路 |
| `ExecutorFilterChain` | 驱动过滤器数组；提供 `getExecutor()`、`getProperties()`、`getFilters()`、`getSize()`、`getPosition()` |

### 工具类

| 方法 | 说明 |
|---|---|
| `Executors.getDelegate(CachingExecutor)` | 反射读取 `CachingExecutor#delegate` |
| `Plugins.getSignatureMap(...)` | 校验并构建 `@Intercepts`/`@Signature` 映射，仅接受 `Executor`、`ParameterHandler`、`ResultSetHandler`、`StatementHandler` 四类目标 |
| `Plugins.getPlugin(Object proxy)` | 从 JDK 代理中提取 MyBatis `Plugin` |
| `Plugins.TARGET_CLASSES` | 四个合法拦截目标类型 |

### 配置属性

| 属性 | 类型 | 默认值 | 含义 |
|---|---|---|---|
| `microsphere.mybatis.enabled` | `boolean` | `true` | Spring Boot / Spring Cloud 自动配置的总开关（`PropertyConstants.MICROSPHERE_MYBATIS_ENABLED_PROPERTY_NAME`） |

## 代码示例

### 1. 指标统计过滤器（接口 javadoc 示例）

```java
public class MetricsExecutorFilter implements ExecutorFilter {

    @Override
    public int update(MappedStatement ms, Object parameter, ExecutorFilterChain chain)
            throws SQLException {
        long start = System.currentTimeMillis();
        int rows = chain.update(ms, parameter);       // 续链
        System.out.println("update took " + (System.currentTimeMillis() - start) + " ms");
        return rows;
    }
}
```

### 2. 审计拦截器

```java
public class AuditExecutorInterceptor implements ExecutorInterceptor {

    @Override
    public void beforeUpdate(InterceptorContext<Executor> context, MappedStatement ms, Object parameter) {
        context.setStartTime(System.currentTimeMillis());
    }

    @Override
    public void afterUpdate(InterceptorContext<Executor> context, MappedStatement ms, Object parameter,
                            Integer result, SQLException failure) {
        // failure 非空时 result 为 null
    }
}
```

### 3. 注册到原生 MyBatis `Configuration`

```java
Configuration configuration = MyBatisUtils.getConfiguration("META-INF/mybatis/config.xml", "testing");
configuration.addInterceptor(
    new InterceptingExecutorInterceptor(
        of(new MetricsExecutorFilter(), new LoggingExecutorFilter()),
        new AuditExecutorInterceptor()));
```

### 4. 真实测试用例（core 模块 `InterceptingExecutorInterceptorTest`）

```java
public class InterceptingExecutorInterceptorTest extends AbstractMapperTest {

    @Override
    protected void customize(Configuration configuration) {
        configuration.addInterceptor(createInterceptingExecutorInterceptor());
        configuration.addInterceptor(new InterceptingExecutorInterceptor(of(new LoggingExecutorFilter())));

        InterceptingExecutorInterceptor interceptor =
            new InterceptingExecutorInterceptor(of(), new ThrowingErrorExecutorInterceptor());
        Properties properties = new Properties();
        properties.setProperty("test.class", this.getClass().getName());
        interceptor.setProperties(properties);        // 经 InterceptorContext#getProperties() 读取
        configuration.addInterceptor(interceptor);
    }

    private InterceptingExecutorInterceptor createInterceptingExecutorInterceptor() {
        return new InterceptingExecutorInterceptor(of(new TestExecutorFilter()),
                new LoggingExecutorInterceptor(), new TestInterceptorContextExecutorInterceptor());
    }
}
```

> 抛异常的 `ExecutorInterceptor` 不会破坏 SQL 执行：`InterceptorsExecutorFilterAdapter`
> 会捕获并记录每个回调异常（warn），示例 4 因此得以安全运行。

## 排序与异常语义

1. 过滤器与适配器内的拦截器均按 `Prioritized` 升序排序。
2. 过滤器异常向上传播（SQL 错误即 `SQLException`）。
3. 拦截器回调异常被吞掉并记录日志。
4. 每次 `Executor` 方法调用都会新建 `ExecutorFilterChain`（position 归零），
   过滤器实例字段不宜保存单次调用状态——请使用 `InterceptorContext` 属性。

## 另见

- [spring 模块](spring.md) —— 把过滤器/拦截器自动注册为 Spring Bean
- [spring-boot 模块](spring-boot.md) —— 同上并自动配置
- [开发者文档：core](../../../developer-guide/current/zh/core.md) —— 源码分析与时序图
