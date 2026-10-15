# microsphere-mybatis 1.x —— 开发者文档（`release-1.x`）

[English](../README.md) · [← 开发者文档总索引](../../README.md) · [用户文档（1.x）](../../../user-guide/1.x/zh/README.md)

版本线：**0.1.x**（分支顶端 `revision` = `0.1.17-SNAPSHOT`），Java 8 基线，
Spring 5 / Spring Boot 2 / Spring Cloud Hoxton–2021 代际。

实现与 [`current` 线](../../current/zh/README.md) 几乎完全镜像。整分支对比仅
**六个源文件** 不同，且不改变任何行为。因此本目录文档只记录差异，
完整的架构与源码分析请回看 `current/`。

## 已核实的源码差异（`release` vs `release-1.x`）

| 文件 | 差异 | 原因 |
|---|---|---|
| `core/…/InterceptingExecutorInterceptor.java` | 传统 `instanceof` + 强转，替代模式匹配 `instanceof` | Java 8 目标（模式匹配需 16+） |
| `core/…/Plugins.java` | 使用 `org.apache.ibatis.util.MapUtil.computeIfAbsent(map, k, fn)` 而非 `Map#computeIfAbsent` | 编写时使用了 MyBatis 自有工具类；语义等价 |
| `test/…/MyBatisTestExtension.java` | 传统 `instanceof AutoCloseable` + 强转 | Java 8 目标 |
| `test/…/MyBatisTestExtensionTest.java` | 额外的 `@MyBatisRuntime private static …` 字段（Configuration、Environment、SqlSessionFactory、DataSource、Properties、SqlSession、Transaction） | 仅测试夹具漂移 |
| `spring-boot` 资源 | **新增** `META-INF/spring.factories`（`EnableAutoConfiguration=…`），同时**保留** `AutoConfiguration.imports` | Boot 2.x（<2.7）只读 `spring.factories` |
| `spring-cloud` 资源 | 同样的双重注册 | 同上 |

POM 差异：`pom.xml`（父级 `microsphere-spring-cloud-parent` 0.1.26，无 `java.version`
属性覆盖 → 继承 Java 8 目标）与 `microsphere-mybatis-parent/pom.xml`
（mybatis 3.5.19、mybatis-spring 2.1.2、mybatis-spring-boot-starter 2.1.2）。

## 模块文档

| 模块 | 文档 | 与 current 的差异 |
|---|---|---|
| utils | [utils.md](utils.md) | 无 |
| core | [core.md](core.md) | 2 个文件，仅 Java-8 语法 |
| test | [test.md](test.md) | 1 个主文件 + 1 个测试文件，仅语法 |
| spring | [spring.md](spring.md) | 无 |
| spring-test | [spring-test.md](spring-test.md) | 无 |
| spring-boot | [spring-boot.md](spring-boot.md) | 双重自动配置注册 |
| spring-cloud | [spring-cloud.md](spring-cloud.md) | 双重自动配置注册 |
| parent / BOM | [parent-and-dependencies.md](parent-and-dependencies.md) | 0.1.x 代际版本 |

另见：[architecture.md](architecture.md) —— 分层与 current 相同，仅外部依赖代际变化。

## CI 矩阵（1.x）

`maven-build.yml`：`java: [8, 11, 17, 21, 25]` × `spring-cloud-hoxton / 2020 / 2021`
（15 种组合）—— 对比 current 的 `[17, 21, 25]` × `2022–2025`。
