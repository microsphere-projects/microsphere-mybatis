# microsphere-mybatis-core — User Guide

[中文](zh/core.md) · [Developer Guide](../../developer-guide/current/core.md) · [← Index](../README.md)

The core module upgrades MyBatis' plugin system for the `Executor` interface: instead of a
single JDK-proxy `Interceptor`, it introduces an ordered chain of **`ExecutorFilter`s**
(Servlet-style, can observe results and failures) and **`ExecutorInterceptor`s**
(before/after callbacks), bridged into MyBatis by one standard `Interceptor` implementation.

## Version Information

| Item | Value |
|---|---|
| Artifact | `io.github.microsphere-projects:microsphere-mybatis-core` |
| Documented version | `0.2.16` (branch `release`, 0.2.x line) |
| Java baseline | 17 |
| Requires | `microsphere-mybatis-utils`, MyBatis `3.5.19` (optional at compile-time) |
| Compatibility | MyBatis 3.5.x; runs identically under plain MyBatis, mybatis-spring and Spring Boot |
| API stability | All public types are `@since 1.0.0`; 0.2.x is API-stable |

## Installation

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-mybatis-core</artifactId>
    <!-- version managed by microsphere-mybatis-dependencies BOM -->
</dependency>
```

## Concepts

- **`ExecutorFilter`** — wraps an `Executor` operation and explicitly continues the chain via
  `ExecutorFilterChain`. It sees inputs, the return value and the `SQLException`, and may
  short-circuit (never call `chain.xxx(...)`), decorate or replace results.
- **`ExecutorInterceptor`** — declarative `beforeXxx` / `afterXxx` callbacks. Simpler to write,
  cannot veto the call. Failures inside one interceptor are logged, never propagated.
- **`InterceptingExecutorInterceptor`** — the only object you register with MyBatis
  (`configuration.addInterceptor(...)`). It wraps the target `Executor` into an
  `InterceptingExecutor` carrying all filters.
- Both extension interfaces extend `io.microsphere.lang.Prioritized`; lower `getPriority()` runs
  first (filters sort ascending in the chain).

## Intercepted Operations

Every operation of `org.apache.ibatis.session.Executor` except `flushStatements`, `isCached`
and `clearLocalCache` is routed through the chain: `update`, both `query` overloads,
`queryCursor`, `commit`, `rollback`, `createCacheKey`, `deferLoad`, `getTransaction`, `close`.

## API Reference

### `io.microsphere.mybatis.executor.ExecutorFilter`

All methods have pass-through defaults — override only what you need.

| Method | Return | Notes |
|---|---|---|
| `int update(MappedStatement ms, Object parameter, ExecutorFilterChain chain)` | affected rows | intercepts `Executor#update` |
| `<E> List<E> query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql, chain)` | result list | the 6-arg cached query |
| `<E> List<E> query(ms, parameter, rowBounds, resultHandler, chain)` | result list | the 4-arg query |
| `<E> Cursor<E> queryCursor(ms, parameter, rowBounds, chain)` | cursor | streaming query |
| `void commit(boolean required, chain)` / `void rollback(boolean required, chain)` | — | transaction boundaries |
| `CacheKey createCacheKey(ms, parameter, rowBounds, boundSql, chain)` | cache key | second-level cache key |
| `void deferLoad(ms, resultObject, property, key, targetType, chain)` | — | lazy nested loads |
| `Transaction getTransaction(chain)` | transaction | |
| `void close(boolean forceRollback, chain)` | — | executor shutdown |

### `io.microsphere.mybatis.executor.ExecutorInterceptor`

Symmetric callback pairs, all optional (default no-ops):

| Before | After (`failure` is non-`null` when the call threw) |
|---|---|
| `beforeUpdate(context, ms, parameter)` | `afterUpdate(context, ms, parameter, result, failure)` |
| `beforeQuery(context, ms, parameter, rowBounds, resultHandler, cacheKey, boundSql)` | `afterQuery(…, result, failure)` |
| `beforeQueryCursor(context, ms, parameter, rowBounds)` | `afterQueryCursor(…, result, failure)` |
| `beforeCommit(context, required)` / `beforeRollback(context, required)` | `afterCommit(…, failure)` / `afterRollback(…, failure)` |
| `beforeCreateCacheKey(…)` / `beforeDeferLoad(…)` / `beforeGetTransaction(context)` | matching `afterXxx(…, failure)` |
| `beforeClose(context, forceRollback)` | `afterClose(context, forceRollback)` |

### `io.microsphere.mybatis.plugin.InterceptorContext<Executor>`

Passed to every callback:

| Method | Description |
|---|---|
| `Executor getTarget()` | the intercepted (delegate) executor |
| `Properties getProperties()` | properties from `Interceptor#setProperties`, may be `null` |
| `setStartTime(Long)` / `getStartTime()` | timing support, fluent setter |
| `setAttribute(name, value)` / `getAttribute(name[, default])` / `hasAttribute` / `removeAttribute(s)` | per-invocation attribute map — share data between `before` and `after` |
| `Map<String,Object> getAttributes()` | read-only view |

### `io.microsphere.mybatis.plugin.InterceptingExecutorInterceptor`

```java
new InterceptingExecutorInterceptor(ExecutorFilter[] filters, ExecutorInterceptor... interceptors)
```

- At least one filter **or** interceptor is required (`IllegalArgumentException` otherwise).
- Any supplied `ExecutorInterceptor`s are wrapped into one `InterceptorsExecutorFilterAdapter`
  and merged with the filters, sorted by `Prioritized` priority.
- `plugin(Object)` (invoked by MyBatis) returns an `InterceptingExecutor` wrapping the executor;
  when the target is a `CachingExecutor`, its internal delegate is unwrapped first so filters
  observe the real SQL execution, and the result is re-wrapped in a fresh `CachingExecutor`.
- Registering several `InterceptingExecutorInterceptor` instances is safe: their filters and
  properties are merged into the existing `InterceptingExecutor`.
- `intercept(Invocation)` is intentionally not used — it logs a warning and proceeds.

### Built-in Implementations

| Class | Role |
|---|---|
| `LoggingExecutorFilter` | Logs every filter operation at `debug` before passing through the chain |
| `LoggingExecutorInterceptor` | Logs every `beforeXxx`/`afterXxx` callback at `debug` |
| `InterceptingExecutor` | The `Executor` decorator; `buildChain()` creates a fresh `ExecutorFilterChain` per invocation |
| `ExecutorFilterChain` | Drives the filter array; also exposes `getExecutor()`, `getProperties()`, `getFilters()`, `getSize()`, `getPosition()` |

### Utilities

| Class / Method | Description |
|---|---|
| `Executors.getDelegate(CachingExecutor)` | Reflectively reads `CachingExecutor#delegate` |
| `Plugins.getSignatureMap(Interceptor \| Class)` | Validates and builds the `@Intercepts`/`@Signature` map; accepts only `Executor`, `ParameterHandler`, `ResultSetHandler`, `StatementHandler` targets |
| `Plugins.getPlugin(Object proxy)` | Extracts the MyBatis `Plugin` from a JDK proxy |
| `Plugins.TARGET_CLASSES` | The four legal interceptor target types |

### Configuration Property

| Property | Type | Default | Meaning |
|---|---|---|---|
| `microsphere.mybatis.enabled` | `boolean` | `true` | Master switch consumed by the Spring Boot / Spring Cloud auto-configurations (`PropertyConstants.MICROSPHERE_MYBATIS_ENABLED_PROPERTY_NAME`) |

## Code Examples

### 1. A metrics filter (from the interface javadoc)

```java
public class MetricsExecutorFilter implements ExecutorFilter {

    @Override
    public int update(MappedStatement ms, Object parameter, ExecutorFilterChain chain)
            throws SQLException {
        long start = System.currentTimeMillis();
        int rows = chain.update(ms, parameter);       // continue the chain
        System.out.println("update took " + (System.currentTimeMillis() - start) + " ms");
        return rows;
    }
}
```

### 2. An audit interceptor

```java
public class AuditExecutorInterceptor implements ExecutorInterceptor {

    @Override
    public void beforeUpdate(InterceptorContext<Executor> context, MappedStatement ms, Object parameter) {
        context.setStartTime(System.currentTimeMillis());
    }

    @Override
    public void afterUpdate(InterceptorContext<Executor> context, MappedStatement ms, Object parameter,
                            Integer result, SQLException failure) {
        // result == null when failure != null
    }
}
```

### 3. Registering with plain MyBatis `Configuration`

```java
Configuration configuration = MyBatisUtils.getConfiguration("META-INF/mybatis/config.xml", "testing");
configuration.addInterceptor(
    new InterceptingExecutorInterceptor(
        of(new MetricsExecutorFilter(), new LoggingExecutorFilter()),
        new AuditExecutorInterceptor()));
```

### 4. Real test-case usage (`InterceptingExecutorInterceptorTest`, core module)

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
        interceptor.setProperties(properties);        // readable via InterceptorContext#getProperties()
        configuration.addInterceptor(interceptor);
    }

    private InterceptingExecutorInterceptor createInterceptingExecutorInterceptor() {
        return new InterceptingExecutorInterceptor(of(new TestExecutorFilter()),
                new LoggingExecutorInterceptor(), new TestInterceptorContextExecutorInterceptor());
    }
}
```

> A throwing `ExecutorInterceptor` never breaks SQL execution: `InterceptorsExecutorFilterAdapter`
> catches and logs every callback failure (`warn`), which is what makes test-case 4 above safe.

## Ordering & Failure Semantics

1. Filters are sorted ascending by `Prioritized` priority; interceptors inside the adapter likewise.
2. Filter exceptions propagate (SQL errors are `SQLException`s).
3. Interceptor callback exceptions are swallowed and logged.
4. Each `Executor` method invocation gets a fresh `ExecutorFilterChain` (position resets to 0),
   so filters must not keep per-invocation state in instance fields — use `InterceptorContext`
   attributes instead.

## See Also

- [spring module](spring.md) — auto-registers filters/interceptors as Spring beans
- [spring-boot module](spring-boot.md) — auto-configuration for the same
- [Developer Guide: core](../../developer-guide/current/core.md) — source analysis & sequence diagrams
