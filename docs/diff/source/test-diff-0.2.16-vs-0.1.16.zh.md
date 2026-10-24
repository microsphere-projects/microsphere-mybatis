# `src/test` 差异对比：`0.2.16` vs `0.1.16`

English version: [`test-diff-0.2.16-vs-0.1.16.md`](./test-diff-0.2.16-vs-0.1.16.md)

逐个 Maven 模块对比本仓库最新两个 tag 的测试代码（`src/test`，含 Java 测试源码与测试资源）。

- [对比范围与方法](#对比范围与方法)
- [模块匹配矩阵](#模块匹配矩阵)
- [过滤后的实现差异](#过滤后的实现差异)
- [已排除的差异](#已排除的差异)
- [未产生分歧的测试要素](#未产生分歧的测试要素)
- [结论](#结论)
- [复现方式](#复现方式)

## 对比范围与方法

| 项目 | 取值 |
|---|---|
| Tag 1 | `0.2.16` → commit `c183692`（`release` / `main`，0.2.x 线，Java 17 + Spring Boot 3.x） |
| Tag 2 | `0.1.16` → commit `4056d41`（`release-1.x` / `dev-1.x`，0.1.x 线，Java 8 + Spring Boot 2.x） |
| 对比路径 | `<module>/src/test/**`（测试 Java 源码**与** `src/test/resources`） |
| 对比方式 | 树对树 diff，开启重命名检测（`git diff -M 0.1.16 0.2.16`） |
| 模块匹配 | 按 Maven 模块名匹配；只存在于单个 tag 的模块忽略不计 |

沿用与 `src/main` 对比一致的两类过滤规则：

1. **Java 8 与 Java 17+ 的语法及 API 差异** —— 本项对比中最关键，因为 JUnit 5 测试类大量使用
   `@Nested`，而「内部类中允许 static 成员」是 Java 16+ 才具备的能力。
2. **Java EE API 与 Jakarta EE API 的差异** —— 同样**不存在**：两个 tag 测试源码中唯一的
   `javax`/`jakarta` 导入是 `javax.sql.DataSource`，而它属于 JDK 包。

## 模块匹配矩阵

| Maven 模块 | 两侧均存在 | Java 文件数 `0.1.16` | Java 文件数 `0.2.16` | 资源文件数 `0.1.16` | 资源文件数 `0.2.16` | 结果 |
|---|---|---|---|---|---|---|
| `microsphere-mybatis-core` | 是 | 15 | 15 | 0 | 0 | 完全一致 |
| `microsphere-mybatis-utils` | 是 | 1 | 1 | 3 | 3 | 完全一致 |
| `microsphere-mybatis-spring` | 是 | 3 | 3 | 3 | 3 | 完全一致 |
| `microsphere-mybatis-spring-test` | 是 | 3 | 3 | 0 | 0 | 完全一致 |
| `microsphere-mybatis-spring-boot` | 是 | 3 | 3 | 0 | 0 | 完全一致 |
| `microsphere-mybatis-spring-cloud` | 是 | 2 | 2 | 0 | 0 | 完全一致 |
| `microsphere-mybatis-test` | 是 | 4 | 4 | 0 | 0 | 1 个 Java 文件有差异（已排除） |
| `microsphere-mybatis-dependencies` | 是 | 0（纯 POM） | 0（纯 POM） | 0 | 0 | 无可对比内容 |
| `microsphere-mybatis-parent` | 是 | 0（纯 POM） | 0（纯 POM） | 0 | 0 | 无可对比内容 |
| **合计** | **9 / 9** | **31** | **31** | **6** | **6** | **37 vs 37 个文件：1 个涉及改动，过滤后保留 0 项** |

## 过滤后的实现差异

**没有差异。** 剔除下表中因 JDK 基线导致的改动后，两条发布线的测试源码与测试资源字节一致 ——
同样是 37 个文件，断言、夹具、覆盖范围完全相同。

## 已排除的差异

| Maven 模块 | 文件 | 区块 | `0.1.16`（Java 8） | `0.2.16`（Java 17） | 排除类别 |
|---|---|---|---|---|---|
| `microsphere-mybatis-test` | `junit/jupiter/MyBatisTestExtensionTest.java` | 区块 1（`@@ -60,42 +59,0`）与区块 2（`@@ -153,0 +112,46`） | 9 个 `@MyBatisRuntime private static` 字段（`Configuration`、`Environment`、`SqlSessionFactory`、`DataSource`、`Properties`、`SqlSession`、`Transaction`、`Executor`、`UserMapper`）以及针对它们断言的 `@Test @DisplayName("Test : Injection on static fields") void test()`，**位于测试类顶层**、排在 `FieldTests` 之前 | 同样的 9 个 static 字段与完全相同的测试方法体，被移入 `@Nested @DisplayName("Test : Injection on static fields") class StaticFieldTests`，位置调整到 `FieldTests` 与 `MethodTests` 之间 | Java 语法 —— JEP 395「内部类中允许静态成员」（Java 15 预览、Java 16 正式）。JUnit 5 要求 `@Nested` 类必须是非静态内部类，而 Java 8 禁止在内部类中声明 `static` 字段，因此「嵌套写法」只有在 Java 17 线上才可能编译通过 |
| `microsphere-mybatis-test` | `junit/jupiter/MyBatisTestExtensionTest.java` | 区块 3（`@@ -173,9 +177,9`） | `static void assertAll(Configuration …, UserMapper userMapper)` | `void assertAll(Configuration …, UserMapper userMapper)`（改为实例方法，签名同步重新缩进） | 同一重构的连带结果。两处调用点（`FieldTests#test` 与 `MethodTests#test`）都处于 `@Nested` 内部类的实例上下文、本来就持有外部实例，因此 `static` 并非必需 —— 属于风格差异而非功能差异，方法体内部断言完全未变 |

该文件在两线上的版本来自**各自独立编写**的同一特性提交，而非 cherry-pick，这也正是 Java 8 适配痕迹
体现在类结构上的原因：

| Tag | 引入该测试文件的提交 |
|---|---|
| `0.2.16` | `bdc3f31` "Add JUnit5 MyBatis test extension and utils"（2026-05-16），随后由 `69db13f` 扩展 |
| `0.1.16` | `0b616a0` "Add MyBatis JUnit Jupiter test utilities"（2026-05-16） |

被断言的内容完全一致：`assertNotNull(configuration)`、
`assertSame(environment, configuration.getEnvironment())`、
`assertSame(configuration, sqlSessionFactory.getConfiguration())`、
`assertSame(dataSource, environment.getDataSource())`、`assertNotNull(properties)`，以及对 `sqlSession`、
`transaction`、`executor`、`userMapper` 的 `assertNull` —— 这份清单正是「静态（before-all）上下文中哪些
`@MyBatisRuntime` 组件可被解析」的行为文档。

## 未产生分歧的测试要素

| 测试资产 | 两个 tag 上的状态 | 意义 |
|---|---|---|
| `microsphere-mybatis-spring/src/test/resources/META-INF/spring.factories` | 均存在且一致 | 这正是 `src/main` 差异的镜像：0.2.x 上 `spring.factories` **仅作为测试夹具**保留，供 `EnableMyBatis.sources()` 的 `SPRING_FACTORIES` 发现路径使用。可见 0.2.x 从 `src/main` 删除它，针对的是生产注册路径，而不是说不认识这种文件格式 |
| `microsphere-mybatis-spring/src/test/resources/META-INF/services/io.microsphere.mybatis.executor.ExecutorFilter` 与 `…ExecutorInterceptor` | 均存在且一致 | `JAVA_SERVICE_PROVIDER` 来源的 Java SPI 夹具与依赖代际无关 |
| `microsphere-mybatis-utils/src/test/resources/META-INF/mybatis/{test-config.xml,test-mybatis.properties,test-scripts.sql}` | 均存在且一致 | MyBatis 夹具共享，两线校验的是同一份表结构与数据 |
| `microsphere-mybatis-core/src/test/**`（15 个文件，含 `LoggingExecutor`、`NoOpInterceptor`、`TestExecutorFilter`、各类抛异常的过滤器以及 `PluginsTest`） | 均存在且一致 | 拦截器/插件契约测试在 MyBatis-Spring 2.x → 4.x 升级中保持不变 |
| `MyBatisAutoConfigurationTest` / `…IntegrationTest`（Boot 与 Cloud，共 5 个文件） | 均存在且一致 | 同一组断言同时校验 Spring Boot 2.x 与 3.x 的自动配置，这正是 `AutoConfiguration.imports` 路径等价的证据 |

## 结论

1. **两条发布线的测试套件等价。** 两侧各 37 个文件，只有 1 个文件被改动，而该改动只是同一测试设计在
   Java 基线上的不同表达 —— 过滤后不存在任何功能性分歧。
2. **static 字段的摆放位置是唯一结构可见的差异**，且由编译器强制：Java 8 线必须把 static
   `@MyBatisRuntime` 字段留在外部类，Java 17 线则可以把它们收进 `@Nested StaticFieldTests`。嵌套写法更
   整洁，但在不抬高 0.1.x 基线的前提下无法回迁。
3. **覆盖等价性可由 diff 直接证明**：夹具、SPI 文件与断言全部字节一致。因此两线之间若出现行为差异，
   只可能来自依赖代际，而不可能来自测试写法的不同。

## 复现方式

```bash
# src/test 全貌：只有 1 个文件
git diff -M --stat        0.1.16 0.2.16 -- '*/src/test/*'
git diff -M --name-status 0.1.16 0.2.16 -- '*/src/test/*'

# 该文件的三个改动区块
git diff -M -U0 0.1.16 0.2.16 -- '*/src/test/*' | grep -E '^(diff --git|@@)'

# 确认测试资源（含 spring.factories）完全一致
git ls-tree -r --name-only 0.1.16 -- microsphere-mybatis-spring/src/test/resources
git ls-tree -r --name-only 0.2.16 -- microsphere-mybatis-spring/src/test/resources

# 直接比对两个版本的类结构
git show 0.1.16:microsphere-mybatis-test/src/test/java/io/microsphere/mybatis/test/junit/jupiter/MyBatisTestExtensionTest.java | grep -nE 'class |assertAll|private static'
git show 0.2.16:microsphere-mybatis-test/src/test/java/io/microsphere/mybatis/test/junit/jupiter/MyBatisTestExtensionTest.java | grep -nE 'class |assertAll|private static'

# 两侧各自的来源提交
git log -1 bdc3f31   # 0.2.x：Add JUnit5 MyBatis test extension and utils
git log -1 0b616a0   # 0.1.x：Add MyBatis JUnit Jupiter test utilities
```

## 相关文档

- [`main-diff-0.2.16-vs-0.1.16.zh.md`](./main-diff-0.2.16-vs-0.1.16.zh.md) —— `src/main` 对应篇
- [文档首页](../../zh/README.md)
- [用户文档](../../user-guide/zh/README.md) · [开发者文档](../../developer-guide/zh/README.md)
- 模块级源码分析：
  [test](../../developer-guide/current/zh/test.md) ·
  [core](../../developer-guide/current/zh/core.md) ·
  [spring](../../developer-guide/current/zh/spring.md) ·
  [utils](../../developer-guide/current/zh/utils.md)

## 许可

本文档基于 [Apache License, Version 2.0](../../../LICENSE) 发布。
