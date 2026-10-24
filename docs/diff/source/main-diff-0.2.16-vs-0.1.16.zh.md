# `src/main` 差异对比：`0.2.16` vs `0.1.16`

English version: [`main-diff-0.2.16-vs-0.1.16.md`](./main-diff-0.2.16-vs-0.1.16.md)

逐个 Maven 模块对比本仓库最新两个 tag 的生产代码（`src/main`，含 Java 源码与资源文件）。

- [对比范围与方法](#对比范围与方法)
- [模块匹配矩阵](#模块匹配矩阵)
- [过滤后的实现差异](#过滤后的实现差异)
- [已排除的差异](#已排除的差异)
- [POM 与依赖背景](#pom-与依赖背景)
- [结论](#结论)
- [复现方式](#复现方式)

## 对比范围与方法

| 项目 | 取值 |
|---|---|
| Tag 1 | `0.2.16` → commit `c183692`（`release` / `main`，0.2.x 线，Java 17 + Spring Boot 3.x） |
| Tag 2 | `0.1.16` → commit `4056d41`（`release-1.x` / `dev-1.x`，0.1.x 线，Java 8 + Spring Boot 2.x） |
| 对比路径 | `<module>/src/main/**`（Java 源码**与**资源文件） |
| 对比方式 | 树对树 diff，开启重命名检测（`git diff -M 0.1.16 0.2.16`） |
| 模块匹配 | 按 Maven 模块名匹配；只存在于单个 tag 的模块忽略不计 |

以下两类差异被过滤，因为它们属于平台基线差异，而非实现选择：

1. **Java 8 与 Java 17+ 的语法及 API 差异** —— `instanceof` 模式匹配、内部类中的 static 成员等
   Java 8 `source/target` 无法编译的写法。基线证据：0.2.x 根 POM 声明了
   `<java.version>17</java.version>`，而 0.1.x 完全没有声明 `java.version`（Java 8 基线继承自外部父
   POM `microsphere-spring-cloud-parent:0.1.26`）；CI 矩阵也印证了这一点 ——
   `0.1.16` 为 `java: [ '8', '11', '17', '21', '25' ]`，`0.2.16` 为 `java: [ '17', '21', '25' ]`。
2. **Java EE API 与 Jakarta EE API 的差异** —— 经核查**源码中根本不存在此类差异**：两个 tag 中唯一的
   `javax.*`/`jakarta.*` 导入是 `javax.sql.DataSource`（14 个文件，两侧完全一致），而 `javax.sql`
   属于 JDK，不在 Java EE → Jakarta EE 改名范围内。因此 0.2.x 线的 `javax` → `jakarta` 迁移完全由
   Spring/MyBatis 的传递依赖承载，本仓库代码无需改动。

两个 tag 发布的 Maven 模块完全相同（9 个），因此按匹配规则没有任何模块被忽略。

## 模块匹配矩阵

| Maven 模块 | 两侧均存在 | Java 文件数 `0.1.16` | Java 文件数 `0.2.16` | 资源文件数 `0.1.16` | 资源文件数 `0.2.16` | 结果 |
|---|---|---|---|---|---|---|
| `microsphere-mybatis-core` | 是 | 12 | 12 | 0 | 0 | 2 个 Java 文件有差异（1 个已排除，1 个保留） |
| `microsphere-mybatis-utils` | 是 | 1 | 1 | 0 | 0 | 完全一致 |
| `microsphere-mybatis-spring` | 是 | 8 | 8 | 0 | 0 | 完全一致 |
| `microsphere-mybatis-spring-test` | 是 | 3 | 3 | 0 | 0 | 完全一致 |
| `microsphere-mybatis-spring-boot` | 是 | 3 | 3 | 2 | 1 | 删除 1 个资源文件（`spring.factories`） |
| `microsphere-mybatis-spring-cloud` | 是 | 1 | 1 | 3 | 2 | 删除 1 个资源文件（`spring.factories`） |
| `microsphere-mybatis-test` | 是 | 26 | 26 | 9 | 9 | 1 个 Java 文件有差异（已排除） |
| `microsphere-mybatis-dependencies` | 是 | 0（纯 POM） | 0（纯 POM） | 0 | 0 | 无可对比内容 |
| `microsphere-mybatis-parent` | 是 | 0（纯 POM） | 0（纯 POM） | 0 | 0 | 无可对比内容 |
| **合计** | **9 / 9** | **54** | **54** | **14** | **12** | **68 vs 66 个文件：5 个涉及改动，过滤后保留 3 项** |

## 过滤后的实现差异

| # | Maven 模块 | 文件 | 类型 | `0.1.16` 实现 | `0.2.16` 实现 | 原因解释 |
|---|---|---|---|---|---|---|
| 1 | `microsphere-mybatis-core` | `plugin/Plugins.java`（第 115 行） | API 调用替换 | `Set<Method> methods = computeIfAbsent(signatureMap, targetType, k -> new HashSet<>());`，并在第 41 行 `import static org.apache.ibatis.util.MapUtil.computeIfAbsent;` | `Set<Method> methods = signatureMap.computeIfAbsent(targetType, k -> new HashSet<>());`，静态导入删除 | **API 兼容 / 依赖治理。** `org.apache.ibatis.util.MapUtil` 是 MyBatis 的内部工具类，仅为兼容历史 JDK 基线而保留；`java.util.Map#computeIfAbsent` 是 JDK 8 的默认方法，0.1.x 线同样可以使用。因此 commit `9c2cd7a`（"Use Map.computeIfAbsent in Plugins"）在 0.2.x 线统一改用 JDK `Map` API，去掉对 MyBatis 非对外工具类的编译期依赖。行为完全一致：`signatureMap` 仍是固定容量的 `HashMap`，按被拦截目标类型各装入一个 `HashSet`。 |
| 2 | `microsphere-mybatis-spring-boot` | `src/main/resources/META-INF/spring.factories` | 仅存在于 `0.1.16` | 2 行：`org.springframework.boot.autoconfigure.EnableAutoConfiguration=\ io.microsphere.mybatis.spring.boot.autoconfigure.MyBatisAutoConfiguration` | 文件不存在 | **Spring Boot 2.x 向下兼容。** Spring Boot 自 2.7 起从 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 读取自动配置注册，并在 3.0 移除了 `spring.factories` 中的 `EnableAutoConfiguration` 键。该 `.imports` 文件在**两个 tag 上都存在且字节一致**，所以 0.2.x 只依赖它就够了；0.1.x 线额外保留传统 `spring.factories`（commit `afddc0e`），用于服务那些版本低于 2.7、完全看不到 `.imports` 文件的 Spring Boot 2.x 使用方。 |
| 3 | `microsphere-mybatis-spring-cloud` | `src/main/resources/META-INF/spring.factories` | 仅存在于 `0.1.16` | 2 行：`org.springframework.boot.autoconfigure.EnableAutoConfiguration=\ io.microsphere.mybatis.spring.cloud.autoconfigure.MyBatisCloudAutoConfiguration` | 文件不存在 | 与 #1-2 同理，针对 Spring Cloud 自动配置（commit `61ff309`）：0.1.x 保留这条重复注册路径以兼容 Spring Boot 2.x，0.2.x 因 Spring Boot 3.x 忽略传统键而将其删除。 |

除上述三项外，`src/main` 再无任何实现分歧。尤其值得注意的是，即使依赖代际发生变化，以下区域在两个
tag 上仍然字节一致：

| 一致的区域（`src/main`） | 说明 |
|---|---|
| `microsphere-mybatis-spring` —— 全部 8 个文件（registrar、`@EnableMyBatis`、Bean 定义注册） | MyBatis-Spring 从 `2.1.2` 升到 `4.1.0` 也无需改动 |
| `microsphere-mybatis-test` —— 其余 34 个文件（扩展点、`ComponentResolver` 实现、注解） | JUnit Jupiter API 在两线上用法稳定 |
| `microsphere-mybatis-spring-boot/autoconfigure/MyBatisAutoConfiguration.java` 与两个 `condition/*.java` | 自动配置类本身与依赖代际无关 |
| `microsphere-mybatis-spring-cloud/autoconfigure/MyBatisCloudAutoConfiguration.java`、`META-INF/config/default/features.yaml` | features YAML 一致，仅注册元数据不同 |
| `microsphere-mybatis-utils` —— `MyBatisUtils.java` | 完全一致 |
| `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（两个 Boot 模块） | 完全一致 —— 现代注册路径为两线共用 |

这是整轮对比中最强的信号：MyBatis-Spring 跨大版本升级（2.x → 4.x，下游的 `javax` → `jakarta`
迁移也发生在这一代）**没有要求本仓库做任何源码适配**，因为本库只依赖稳定的
`org.apache.ibatis.*` 契约以及 Spring 的 `BeanDefinitionRegistryPostProcessor` /
`ImportBeanDefinitionRegistrar` 扩展点。

## 已排除的差异

下列内容仅为可追溯性而列出；它们归因于 JDK 基线，因此不计入实现差异。

| Maven 模块 | 文件 | 行号 | `0.1.16`（Java 8） | `0.2.16`（Java 17） | 排除类别 |
|---|---|---|---|---|---|
| `microsphere-mybatis-core` | `plugin/InterceptingExecutorInterceptor.java` | 135、147-148 | `if (target instanceof Executor) { Executor executor = ((Executor) target); }` 以及 `if (delegate instanceof InterceptingExecutor) { InterceptingExecutor previousInterceptingExecutor = (InterceptingExecutor) delegate; }` | `if (target instanceof Executor executor)` 以及 `if (delegate instanceof InterceptingExecutor previousInterceptingExecutor)` | Java 语法（`instanceof` 模式匹配，JEP 397，Java 16+）—— commit `ebfe83c` 明确写明 "No functional behavior changes" |
| `microsphere-mybatis-test` | `junit/jupiter/MyBatisTestExtension.java` | 137 | `if (component instanceof AutoCloseable) { AutoCloseable closeable = (AutoCloseable) component; }` | `if (component instanceof AutoCloseable closeable)` | Java 语法（同一 JEP 397 写法） |

两处改动都是纯粹的消除强制转换：使用的 `Executor`、`InterceptingExecutor`、`AutoCloseable` 值完全相同，
周围的 delegate 解包与 close 逻辑也逐行一致。

## POM 与依赖背景

不结合代际开关就无法理解 `src/main` 的内容。以下 POM 改动虽不在对比路径内，但正是上表达差异的成因。

| POM | `0.1.16` | `0.2.16` | 影响 |
|---|---|---|---|
| `pom.xml`（reactor）parent | `microsphere-spring-cloud-parent:0.1.26` | `microsphere-spring-cloud-parent:0.2.26` | 整体切换平台：Spring Boot 2.x + `javax` + Java 8 → Spring Boot 3.x + `jakarta` + Java 17 |
| `pom.xml` properties | `<revision>0.1.16-SNAPSHOT</revision>` | `<revision>0.2.16-SNAPSHOT</revision>`、`<java.version>17</java.version>` | Java 17 仅在 0.2.x 线显式声明 |
| `microsphere-mybatis-parent/pom.xml` | `mybatis-spring.version=2.1.2`、`mybatis-spring-boot.version=2.1.2` | `mybatis-spring.version=4.1.0`、`mybatis-spring-boot.version=3.0.5` | MyBatis-Spring 大版本代际切换，是 `spring.factories` 差异的直接原因 |
| `microsphere-mybatis-parent/pom.xml` | `mybatis.version=3.5.19`、`cglib.version=3.3.0` | `mybatis.version=3.5.19`、`cglib.version=3.3.0` | 未变 —— 这正是 `microsphere-mybatis-core` 几乎不需要分叉的原因 |

各功能模块自身的依赖声明毫无差异：两个 tag 上所有 `microsphere-mybatis-*/pom.xml` 完全一致。

## 结论

1. **两条发布线在实现上是等价的。** 9 个匹配模块共 68 / 66 个 `src/main` 文件，在过滤掉 Java 基线与
   Jakarta EE 因素后只剩 3 处差异：`Plugins.java` 的一处 JDK `Map` API 替换，以及 0.1.x 独有的两处
   传统 Spring Boot 自动配置注册。
2. **所有保留的差异都是兼容性驱动，而非功能驱动。** 两侧都没有新增 API、没有方法签名变化、没有控制
   流改动，也没有行为分歧。
3. **`Plugins.java` 是唯一的真实代码分歧**，且属于依赖治理类改动，同样可以回带到 0.1.x —— 如果更看重
   两线一致性而非受维护的 Java 8 分支的最小变更量，这是一个候选回迁项。
4. **0.1.x 上的 `spring.factories` 是有意的重复注册，并非遗留陈旧文件** —— 删除它会破坏 Spring Boot
   2.7 以下版本的自动配置发现，而这正是 0.1.x 线服务的用户群。

## 复现方式

```bash
# src/main 全貌（5 个文件：3 项保留，2 项见「已排除的差异」）
git diff -M --stat  0.1.16 0.2.16 -- '*/src/main/*'
git diff -M --name-status 0.1.16 0.2.16 -- '*/src/main/*'

# 逐模块 Java / 资源文件拆分，即匹配矩阵所用的口径
for t in 0.1.16 0.2.16; do
  for m in microsphere-mybatis-*; do
    printf '%-34s %-8s java=%-3s res=%s\n' "$m" "$t" \
      "$(git ls-tree -r --name-only $t -- $m/src/main | grep -c '\.java$')" \
      "$(git ls-tree -r --name-only $t -- $m/src/main | grep -vc '\.java$')"
  done
done

# 得出「Java EE / Jakarta EE 无差异」的检查
git grep -h -E '^import (javax|jakarta)\.' 0.1.16 -- '*/src/*' | sort | uniq -c
git grep -h -E '^import (javax|jakarta)\.' 0.2.16 -- '*/src/*' | sort | uniq -c

# 保留差异背后的提交
git log -1 9c2cd7a   # Use Map.computeIfAbsent in Plugins
git log -1 afddc0e   # Add spring.factories for MyBatis auto-configuration (0.1.x)
git log -1 61ff309   # Add spring.factories for MyBatis Cloud auto-config (0.1.x)
git log -1 ebfe83c   # Use pattern matching for instanceof in interceptor (已排除)
```

## 相关文档

- [`test-diff-0.2.16-vs-0.1.16.zh.md`](./test-diff-0.2.16-vs-0.1.16.zh.md) —— `src/test` 对应篇
- [文档首页](../../zh/README.md)
- [用户文档](../../user-guide/zh/README.md) · [开发者文档](../../developer-guide/zh/README.md)
- 模块级源码分析：
  [core](../../developer-guide/current/zh/core.md) ·
  [spring-boot](../../developer-guide/current/zh/spring-boot.md) ·
  [spring-cloud](../../developer-guide/current/zh/spring-cloud.md) ·
  [test](../../developer-guide/current/zh/test.md)

## 许可

本文档基于 [Apache License, Version 2.0](../../../LICENSE) 发布。
