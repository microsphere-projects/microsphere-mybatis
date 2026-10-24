# `src/test` Diff: `0.2.16` vs `0.1.16`

中文版本：[`test-diff-0.2.16-vs-0.1.16.zh.md`](./test-diff-0.2.16-vs-0.1.16.zh.md)

Compare the test sources (`src/test`, Java code and test resources) of the two newest tags of this
repository, module by Maven module.

- [Scope and method](#scope-and-method)
- [Module matching matrix](#module-matching-matrix)
- [Implementation differences after filtering](#implementation-differences-after-filtering)
- [Excluded differences](#excluded-differences)
- [Test-scope facts that did not diverge](#test-scope-facts-that-did-not-diverge)
- [Conclusions](#conclusions)
- [How to reproduce](#how-to-reproduce)

## Scope and method

| Item | Value |
|---|---|
| Tag 1 | `0.2.16` → commit `c183692` (`release` / `main`, 0.2.x line, Java 17 + Spring Boot 3.x) |
| Tag 2 | `0.1.16` → commit `4056d41` (`release-1.x` / `dev-1.x`, 0.1.x line, Java 8 + Spring Boot 2.x) |
| Compared path | `<module>/src/test/**` (test Java sources **and** `src/test/resources`) |
| Comparison mode | tree-to-tree diff, rename detection enabled (`git diff -M 0.1.16 0.2.16`) |
| Module matching | by Maven module name; a module present in only one tag is ignored |

The same two filter classes as in the `src/main` comparison are applied:

1. **Java 8 versus Java 17+ syntax and API** — most relevant here, because the JUnit 5 test classes
   use `@Nested`, and static members inside an inner class are a Java 16+ capability.
2. **Java EE versus Jakarta EE APIs** — again **no occurrence**: `javax.sql.DataSource` is the only
   `javax`/`jakarta` import in the test sources of both tags, and it is a JDK package.

## Module matching matrix

| Maven module | Matched in both tags | Java files `0.1.16` | Java files `0.2.16` | Resource files `0.1.16` | Resource files `0.2.16` | Result |
|---|---|---|---|---|---|---|
| `microsphere-mybatis-core` | yes | 15 | 15 | 0 | 0 | identical |
| `microsphere-mybatis-utils` | yes | 1 | 1 | 3 | 3 | identical |
| `microsphere-mybatis-spring` | yes | 3 | 3 | 3 | 3 | identical |
| `microsphere-mybatis-spring-test` | yes | 3 | 3 | 0 | 0 | identical |
| `microsphere-mybatis-spring-boot` | yes | 3 | 3 | 0 | 0 | identical |
| `microsphere-mybatis-spring-cloud` | yes | 2 | 2 | 0 | 0 | identical |
| `microsphere-mybatis-test` | yes | 4 | 4 | 0 | 0 | 1 Java file differs (excluded) |
| `microsphere-mybatis-dependencies` | yes | 0 (POM-only) | 0 (POM-only) | 0 | 0 | nothing to compare |
| `microsphere-mybatis-parent` | yes | 0 (POM-only) | 0 (POM-only) | 0 | 0 | nothing to compare |
| **Total** | **9 / 9** | **31** | **31** | **6** | **6** | **37 vs 37 files: 1 touched, 0 retained after filtering** |

## Implementation differences after filtering

**None.** After removing the JDK-baseline-driven hunk described below, the two release lines have
byte-identical test sources and test resources — same 37 files, same assertions, same fixtures, same
coverage.

## Excluded differences

| Maven module | File | Hunks | `0.1.16` (Java 8) | `0.2.16` (Java 17) | Exclusion category |
|---|---|---|---|---|---|
| `microsphere-mybatis-test` | `junit/jupiter/MyBatisTestExtensionTest.java` | 1 (`@@ -60,42 +59,0`) and 2 (`@@ -153,0 +112,46`) | The 9 `@MyBatisRuntime private static` fields (`Configuration`, `Environment`, `SqlSessionFactory`, `DataSource`, `Properties`, `SqlSession`, `Transaction`, `Executor`, `UserMapper`) and the `@Test @DisplayName("Test : Injection on static fields") void test()` that asserts on them live **at the top level** of the test class, before `FieldTests` | The same 9 static fields and the same test method body are moved into `@Nested @DisplayName("Test : Injection on static fields") class StaticFieldTests`, placed between `FieldTests` and `MethodTests` | Java syntax — JEP 395 "Permitted Static Members in Inner Classes" (preview Java 15, standard Java 16). JUnit 5 requires an `@Nested` class to be a non-static inner class, and Java 8 forbids `static` fields inside an inner class, so the nested arrangement is only compilable on the Java 17 line |
| `microsphere-mybatis-test` | `junit/jupiter/MyBatisTestExtensionTest.java` | 3 (`@@ -173,9 +177,9`) | `static void assertAll(Configuration …, UserMapper userMapper)` | `void assertAll(Configuration …, UserMapper userMapper)` (instance method, re-indented signature) | Consequence of the same reorganisation. Both call sites (`FieldTests#test` and `MethodTests#test`) sit in `@Nested` inner-class instance contexts that already have an enclosing instance, so `static` was never required — the difference is stylistic, not functional, and the assertions inside are unchanged |

The file exists on both lines thanks to **independently authored** commits of the same feature rather
than a cherry-pick, which is why the Java-8 adaptation is visible in the shape of the class:

| Tag | Commit that introduced this test file |
|---|---|
| `0.2.16` | `bdc3f31` "Add JUnit5 MyBatis test extension and utils" (2026-05-16), extended by `69db13f` |
| `0.1.16` | `0b616a0` "Add MyBatis JUnit Jupiter test utilities" (2026-05-16) |

Everything that is asserted is identical: `assertNotNull(configuration)`,
`assertSame(environment, configuration.getEnvironment())`,
`assertSame(configuration, sqlSessionFactory.getConfiguration())`,
`assertSame(dataSource, environment.getDataSource())`, `assertNotNull(properties)`, and
`assertNull` on `sqlSession`, `transaction`, `executor` and `userMapper` — the same list that documents
which `@MyBatisRuntime` components are resolvable in a static (before-all) context.

## Test-scope facts that did not diverge

| Test asset | State on both tags | Why it matters |
|---|---|---|
| `microsphere-mybatis-spring/src/test/resources/META-INF/spring.factories` | present and identical | This is the counterpart of the `src/main` difference: `spring.factories` survives on 0.2.x **only as a test fixture** for `EnableMyBatis.sources()` `SPRING_FACTORIES` discovery. Its removal from `src/main` on 0.2.x is therefore about the production registration path, not about the file format being unknown to the 0.2.x code |
| `microsphere-mybatis-spring/src/test/resources/META-INF/services/io.microsphere.mybatis.executor.ExecutorFilter` and `…ExecutorInterceptor` | present and identical | Java SPI fixtures for the `JAVA_SERVICE_PROVIDER` source are generation-agnostic |
| `microsphere-mybatis-utils/src/test/resources/META-INF/mybatis/{test-config.xml,test-mybatis.properties,test-scripts.sql}` | present and identical | The MyBatis fixture set is shared, so both lines validate against the same schema and data |
| `microsphere-mybatis-core/src/test/**` (15 files, incl. `LoggingExecutor`, `NoOpInterceptor`, `TestExecutorFilter`, the throwing filters and `PluginsTest`) | present and identical | The interceptor/plugin contract tests are unchanged across the MyBatis-Spring 2.x → 4.x bump |
| `MyBatisAutoConfigurationTest` / `…IntegrationTest` (Boot and Cloud, 5 files) | present and identical | The same assertions validate auto-configuration on Spring Boot 2.x and 3.x, which is what makes the `AutoConfiguration.imports` path provably equivalent |

## Conclusions

1. **The test suites of the two release lines are equivalent.** 37 files on each side, one touched
   file, and that single difference is a Java-baseline expression of the same test design — no
   retained functional divergence at all.
2. **The static-field test placement is the only structurally visible difference**, and it is forced
   by the compiler: the Java 8 line must keep the static `@MyBatisRuntime` fields in the outer class,
   while the Java 17 line can group them into a `@Nested StaticFieldTests` class. The nested form is
   the tidier one; back-porting it is impossible without raising the 0.1.x baseline.
3. **Coverage parity is provable from the diff**, since fixtures, SPI files and assertions are
   byte-identical. A behavioural difference between the lines can therefore only come from the
   dependency generation, never from a differently-written test.

## How to reproduce

```bash
# The whole src/test picture: exactly one file
git diff -M --stat        0.1.16 0.2.16 -- '*/src/test/*'
git diff -M --name-status 0.1.16 0.2.16 -- '*/src/test/*'

# The three hunks of the one differing file
git diff -M -U0 0.1.16 0.2.16 -- '*/src/test/*' | grep -E '^(diff --git|@@)'

# Confirm the test resources are identical, spring.factories included
git ls-tree -r --name-only 0.1.16 -- microsphere-mybatis-spring/src/test/resources
git ls-tree -r --name-only 0.2.16 -- microsphere-mybatis-spring/src/test/resources

# Compare the two test-class shapes directly
git show 0.1.16:microsphere-mybatis-test/src/test/java/io/microsphere/mybatis/test/junit/jupiter/MyBatisTestExtensionTest.java | grep -nE 'class |assertAll|private static'
git show 0.2.16:microsphere-mybatis-test/src/test/java/io/microsphere/mybatis/test/junit/jupiter/MyBatisTestExtensionTest.java | grep -nE 'class |assertAll|private static'

# The commits behind each side
git log -1 bdc3f31   # 0.2.x: Add JUnit5 MyBatis test extension and utils
git log -1 0b616a0   # 0.1.x: Add MyBatis JUnit Jupiter test utilities
```

## Related documents

- [`main-diff-0.2.16-vs-0.1.16.md`](./main-diff-0.2.16-vs-0.1.16.md) — the `src/main` counterpart
- [Documentation home](../../README.md)
- [User Guide](../../user-guide/README.md) · [Developer Guide](../../developer-guide/README.md)
- Module-level source analysis:
  [test](../../developer-guide/current/test.md) ·
  [core](../../developer-guide/current/core.md) ·
  [spring](../../developer-guide/current/spring.md) ·
  [utils](../../developer-guide/current/utils.md)

## License

This document is released under the [Apache License, Version 2.0](../../../LICENSE).
