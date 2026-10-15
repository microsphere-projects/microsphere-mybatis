# microsphere-mybatis-core (1.x) — Developer Guide

[中文](zh/core.md) · [← 1.x Index](README.md) · [Current analysis](../current/core.md)

Architecture, class responsibilities, and the filter-chain execution model are unchanged —
read [current/core.md](../current/core.md) for the full analysis (`plugin()` unwrapping at
`InterceptingExecutorInterceptor`, `process()`/`iterate()` chain advancement, the
`selectList` sequence diagram, and the pattern table). Only two files differ on this branch,
both for Java-8 language compatibility.

## Source diffs

### 1. `InterceptingExecutorInterceptor#plugin`

```java
// release (Java 17)                      // release-1.x (Java 8)
if (target instanceof Executor executor) {          if (target instanceof Executor) {
                                                     Executor executor = ((Executor) target);
...                                                 ...
if (delegate instanceof InterceptingExecutor         if (delegate instanceof InterceptingExecutor) {
        previousInterceptingExecutor) {                 InterceptingExecutor previousInterceptingExecutor =
                                                            (InterceptingExecutor) delegate;
```

Pattern-matching `instanceof` (JEP 394, Java 16) is replaced by the classic
test-then-cast idiom. Control flow, `CachingExecutor` unwrapping, and filter/properties
merging are identical.

### 2. `Plugins#getSignatureMap`

```java
// release                                          // release-1.x
signatureMap.computeIfAbsent(targetType, k -> new HashSet<>());
// vs MyBatis' own util, semantically equivalent:
org.apache.ibatis.util.MapUtil.computeIfAbsent(signatureMap, targetType, k -> new HashSet<>());
```

`MapUtil.computeIfAbsent` exists in every supported MyBatis line and behaves the same
(compute-if-absent into the map). Note it returns via `map.get(key)` after computing.

## Unchanged semantics worth re-verifying per line

When porting fixes between lines, these are the load-bearing behaviours — identical source
on both branches, but regression-tested separately:

- `InterceptingExecutor.buildChain()` per invocation (no chain reuse across calls).
- `InterceptorsExecutorFilterAdapter` swallowing interceptor callbacks' exceptions into
  `warn` logs; `ExecutorFilter` exceptions propagating.
- Multi-`InterceptingExecutorInterceptor` merge in `plugin()` (filters concatenated,
  `Properties` merged, previous `InterceptingExecutor` unwrapped to its delegate).

## See Also

- [1.x differences overview](README.md)
- [User Guide](../../user-guide/1.x/core.md)
