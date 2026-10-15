# microsphere-mybatis-core（1.x）—— 开发者文档

[English](../core.md) · [← 1.x 索引](README.md) · [current 源码分析](../../current/zh/core.md)

架构、类职责与过滤器链执行模型不变 —— 完整分析见
[current/core.md](../../current/zh/core.md)（`plugin()` 在 `InterceptingExecutorInterceptor`
中的解包、`process()`/`iterate()` 的链式推进、`selectList` 时序图与模式表）。
本分支只有两个文件不同，且均为 Java-8 语言兼容。

## 源码差异

### 1. `InterceptingExecutorInterceptor#plugin`

```java
// release（Java 17）                             // release-1.x（Java 8）
if (target instanceof Executor executor) {          if (target instanceof Executor) {
                                                     Executor executor = ((Executor) target);
...                                                 ...
if (delegate instanceof InterceptingExecutor         if (delegate instanceof InterceptingExecutor) {
        previousInterceptingExecutor) {                 InterceptingExecutor previousInterceptingExecutor =
                                                            (InterceptingExecutor) delegate;
```

模式匹配 `instanceof`（JEP 394，Java 16）被替换为传统的先测后转写法。控制流、
`CachingExecutor` 解包、过滤器/Properties 合并完全一致。

### 2. `Plugins#getSignatureMap`

```java
// release                                        // release-1.x
signatureMap.computeIfAbsent(targetType, k -> new HashSet<>());
// 换成 MyBatis 自有工具类，语义等价：
org.apache.ibatis.util.MapUtil.computeIfAbsent(signatureMap, targetType, k -> new HashSet<>());
```

`MapUtil.computeIfAbsent` 在所有受支持的 MyBatis 版本线中都存在且行为一致
（缺失时计算并放入 map）。注意其实现是计算后再 `map.get(key)` 返回。

## 跨线移植修复时需重新验证的语义

这些是承重行为 —— 两个分支源码相同，但各自独立做回归测试：

- 每次调用都 `buildChain()` 新建链（`InterceptingExecutor` 不跨调用复用链）。
- `InterceptorsExecutorFilterAdapter` 把拦截器回调抛出的异常吞掉并记 `warn` 日志；
  `ExecutorFilter` 的异常则向上传播。
- 多个 `InterceptingExecutorInterceptor` 在 `plugin()` 中的合并（过滤器拼接、
  `Properties` 合并、把先前的 `InterceptingExecutor` 解包到其 delegate）。

## 另见

- [1.x 差异总览](README.md)
- [用户文档](../../../user-guide/1.x/zh/core.md)
