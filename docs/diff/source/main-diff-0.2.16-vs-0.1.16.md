# `src/main` Diff: `0.2.16` vs `0.1.16`

中文版本：[`main-diff-0.2.16-vs-0.1.16.zh.md`](./main-diff-0.2.16-vs-0.1.16.zh.md)

Compare the production sources (`src/main`, Java code and resources) of the two newest tags of this
repository, module by Maven module.

- [Scope and method](#scope-and-method)
- [Module matching matrix](#module-matching-matrix)
- [Implementation differences after filtering](#implementation-differences-after-filtering)
- [Excluded differences](#excluded-differences)
- [Module POM and dependency context](#module-pom-and-dependency-context)
- [Conclusions](#conclusions)
- [How to reproduce](#how-to-reproduce)

## Scope and method

| Item | Value |
|---|---|
| Tag 1 | `0.2.16` → commit `c183692` (`release` / `main`, 0.2.x line, Java 17 + Spring Boot 3.x) |
| Tag 2 | `0.1.16` → commit `4056d41` (`release-1.x` / `dev-1.x`, 0.1.x line, Java 8 + Spring Boot 2.x) |
| Compared path | `<module>/src/main/**` (Java sources **and** resources) |
| Comparison mode | tree-to-tree diff, rename detection enabled (`git diff -M 0.1.16 0.2.16`) |
| Module matching | by Maven module name; a module present in only one tag is ignored |

Two difference classes are filtered out, because they are platform-baseline artifacts and not
implementation choices:

1. **Java 8 versus Java 17+ syntax and API** — `instanceof` pattern matching, static members in inner
   classes, and any other construct the Java 8 `source/target` cannot compile. Evidence for the
   baselines: the 0.2.x root POM declares `<java.version>17</java.version>`, while 0.1.x declares no
   `java.version` at all (the Java 8 baseline is inherited from the external parent
   `microsphere-spring-cloud-parent:0.1.26`); the CI matrices confirm the split —
   `java: [ '8', '11', '17', '21', '25' ]` on `0.1.16` versus `java: [ '17', '21', '25' ]` on `0.2.16`.
2. **Java EE versus Jakarta EE APIs** — checked and **no such difference exists in the sources**: the
   only `javax.*`/`jakarta.*` import in either tag is `javax.sql.DataSource` (14 files, identical on
   both sides), and `javax.sql` belongs to the JDK, not to the Java EE → Jakarta EE rename. The
   `javax` → `jakarta` migration on the 0.2.x line is therefore entirely carried by the transitive
   Spring/MyBatis dependencies, not by this repository's code.

Both tags publish the same 9 Maven modules, so no module was ignored by the matching rule.

## Module matching matrix

| Maven module | Matched in both tags | Java files `0.1.16` | Java files `0.2.16` | Resource files `0.1.16` | Resource files `0.2.16` | Result |
|---|---|---|---|---|---|---|
| `microsphere-mybatis-core` | yes | 12 | 12 | 0 | 0 | 2 Java files differ (1 excluded, 1 retained) |
| `microsphere-mybatis-utils` | yes | 1 | 1 | 0 | 0 | identical |
| `microsphere-mybatis-spring` | yes | 8 | 8 | 0 | 0 | identical |
| `microsphere-mybatis-spring-test` | yes | 3 | 3 | 0 | 0 | identical |
| `microsphere-mybatis-spring-boot` | yes | 3 | 3 | 2 | 1 | 1 resource file (`spring.factories`) removed |
| `microsphere-mybatis-spring-cloud` | yes | 1 | 1 | 3 | 2 | 1 resource file (`spring.factories`) removed |
| `microsphere-mybatis-test` | yes | 26 | 26 | 9 | 9 | 1 Java file differs (excluded) |
| `microsphere-mybatis-dependencies` | yes | 0 (POM-only) | 0 (POM-only) | 0 | 0 | nothing to compare |
| `microsphere-mybatis-parent` | yes | 0 (POM-only) | 0 (POM-only) | 0 | 0 | nothing to compare |
| **Total** | **9 / 9** | **54** | **54** | **14** | **12** | **68 vs 66 files: 5 touched, 3 retained after filtering** |

## Implementation differences after filtering

| # | Maven module | File | Type | `0.1.16` implementation | `0.2.16` implementation | Reason |
|---|---|---|---|---|---|---|
| 1 | `microsphere-mybatis-core` | `plugin/Plugins.java` (line 115) | API call replacement | `Set<Method> methods = computeIfAbsent(signatureMap, targetType, k -> new HashSet<>());` plus `import static org.apache.ibatis.util.MapUtil.computeIfAbsent;` (line 41) | `Set<Method> methods = signatureMap.computeIfAbsent(targetType, k -> new HashSet<>());`, static import removed | **API compatibility / dependency hygiene.** `org.apache.ibatis.util.MapUtil` is a MyBatis internal helper, kept by MyBatis only for historic JDK baselines; `java.util.Map#computeIfAbsent` is a JDK 8 default method, so the 0.1.x line could call it too. Commit `9c2cd7a` ("Use Map.computeIfAbsent in Plugins") therefore standardised on the JDK `Map` API on the 0.2.x line to drop the compile-time dependency on a non-public-facing MyBatis util class. Behaviour is identical: `signatureMap` is still a fixed-capacity `HashMap` populated with one `HashSet` per intercepted target type. |
| 2 | `microsphere-mybatis-spring-boot` | `src/main/resources/META-INF/spring.factories` | File exists only in `0.1.16` | 2 lines: `org.springframework.boot.autoconfigure.EnableAutoConfiguration=\ io.microsphere.mybatis.spring.boot.autoconfigure.MyBatisAutoConfiguration` | file absent | **Backward compatibility with Spring Boot 2.x.** Spring Boot reads auto-configuration registrations from `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` since 2.7 and dropped the `EnableAutoConfiguration` key of `spring.factories` in 3.0. That `.imports` file is present **and byte-identical on both tags**, so 0.2.x needs only it; the 0.1.x line additionally ships the legacy `spring.factories` (commits `afddc0e`) so consumers pinned to Spring Boot 2.x below 2.7 — which cannot see the `.imports` file at all — still get `MyBatisAutoConfiguration` registered. |
| 3 | `microsphere-mybatis-spring-cloud` | `src/main/resources/META-INF/spring.factories` | File exists only in `0.1.16` | 2 lines: `org.springframework.boot.autoconfigure.EnableAutoConfiguration=\ io.microsphere.mybatis.spring.cloud.autoconfigure.MyBatisCloudAutoConfiguration` | file absent | Same as #1-2 for the Spring Cloud auto-configuration (commit `61ff309`): duplicated registration path kept on 0.1.x for Spring Boot 2.x consumers, dropped on 0.2.x because Spring Boot 3.x ignores the legacy key. |

No other production-source divergence exists. In particular, these areas are byte-identical on both
tags even though the dependency generation changed:

| Identical area (`src/main`) | Note |
|---|---|
| `microsphere-mybatis-spring` — all 8 files ( registrars, `@EnableMyBatis`, bean definition registration) | Unchanged despite MyBatis-Spring `2.1.2` → `4.1.0` |
| `microsphere-mybatis-test` — the other 34 files (extension, `ComponentResolver` implementations, annotations) | JUnit Jupiter API usage is stable across both lines |
| `microsphere-mybatis-spring-boot/autoconfigure/MyBatisAutoConfiguration.java` + both `condition/*.java` | The auto-configuration classes themselves are generation-agnostic |
| `microsphere-mybatis-spring-cloud/autoconfigure/MyBatisCloudAutoConfiguration.java`, `META-INF/config/default/features.yaml` | Features YAML identical; only the registration metadata differs |
| `microsphere-mybatis-utils` — `MyBatisUtils.java` | identical |
| `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (both Boot modules) | identical — the modern registration path is shared |

This is the strongest signal in the whole comparison: a major-version bump of MyBatis-Spring
(2.x → 4.x, which is also where the `javax` → `jakarta` transition happens downstream) required
**zero** source adaptation in this repository, because the library only touches the stable
`org.apache.ibatis.*` and Spring `BeanDefinitionRegistryPostProcessor`/`ImportBeanDefinitionRegistrar`
contracts.

## Excluded differences

Listed for traceability; they are attributed to the JDK baseline and are therefore not counted as
implementation differences.

| Maven module | File | Lines | `0.1.16` (Java 8) | `0.2.16` (Java 17) | Exclusion category |
|---|---|---|---|---|---|
| `microsphere-mybatis-core` | `plugin/InterceptingExecutorInterceptor.java` | 135, 147-148 | `if (target instanceof Executor) { Executor executor = ((Executor) target); }` and `if (delegate instanceof InterceptingExecutor) { InterceptingExecutor previousInterceptingExecutor = (InterceptingExecutor) delegate; }` | `if (target instanceof Executor executor)` and `if (delegate instanceof InterceptingExecutor previousInterceptingExecutor)` | Java syntax (pattern matching for `instanceof`, JEP 397, Java 16+) — commit `ebfe83c` explicitly notes "No functional behavior changes" |
| `microsphere-mybatis-test` | `junit/jupiter/MyBatisTestExtension.java` | 137 | `if (component instanceof AutoCloseable) { AutoCloseable closeable = (AutoCloseable) component; }` | `if (component instanceof AutoCloseable closeable)` | Java syntax (same JEP 397 construct) |

Both hunks are pure cast-elimination: the same `Executor`, `InterceptingExecutor` and `AutoCloseable`
values are used in the same way, and the surrounding delegate-unwrapping and close logic is identical.

## Module POM and dependency context

`src/main` content cannot be read without the generation switch that explains it. These POM changes
are outside the compared paths but are the cause of the entries above.

| POM | `0.1.16` | `0.2.16` | Effect |
|---|---|---|---|
| `pom.xml` (reactor) parent | `microsphere-spring-cloud-parent:0.1.26` | `microsphere-spring-cloud-parent:0.2.26` | Switches the whole platform: Spring Boot 2.x + `javax` + Java 8 → Spring Boot 3.x + `jakarta` + Java 17 |
| `pom.xml` properties | `<revision>0.1.16-SNAPSHOT</revision>` | `<revision>0.2.16-SNAPSHOT</revision>`, `<java.version>17</java.version>` | Java 17 declared only on the 0.2.x line |
| `microsphere-mybatis-parent/pom.xml` | `mybatis-spring.version=2.1.2`, `mybatis-spring-boot.version=2.1.2` | `mybatis-spring.version=4.1.0`, `mybatis-spring-boot.version=3.0.5` | MyBatis-Spring major generation; drives the `spring.factories` difference |
| `microsphere-mybatis-parent/pom.xml` | `mybatis.version=3.5.19`, `cglib.version=3.3.0` | `mybatis.version=3.5.19`, `cglib.version=3.3.0` | Unchanged — why `microsphere-mybatis-core` needed almost no divergence |

Individual feature modules declare no dependency-version differences at all: every
`microsphere-mybatis-*/pom.xml` is identical on both tags.

## Conclusions

1. **The two release lines are implementation-equivalent.** Across 68 / 66 `src/main` files in 9
   matched modules, only 3 differences survive the Java-baseline and Jakarta-EE filters: one JDK
   `Map` API substitution in `Plugins.java` and two legacy Spring Boot auto-configuration
   registrations carried only by 0.1.x.
2. **Every retained difference is compatibility-driven, not feature-driven.** No new API, no changed
   method signature, no altered control flow, and no behavioural divergence on either side.
3. **`Plugins.java` is the only genuine code divergence** and it is a dependency-hygiene change that
   could equally be applied to 0.1.x — a candidate for back-porting if consistency between the lines
   is preferred over minimal churn on a maintained Java 8 branch.
4. **`spring.factories` on 0.1.x is deliberate duplication, not staleness** — removing it would break
   Spring Boot consumers older than 2.7, which is exactly the population the 0.1.x line serves.

## How to reproduce

```bash
# The full src/main picture (5 files: 3 retained, 2 excluded above)
git diff -M --stat  0.1.16 0.2.16 -- '*/src/main/*'
git diff -M --name-status 0.1.16 0.2.16 -- '*/src/main/*'

# Per-module Java / resource split, as used in the matching matrix
for t in 0.1.16 0.2.16; do
  for m in microsphere-mybatis-*; do
    printf '%-34s %-8s java=%-3s res=%s\n' "$m" "$t" \
      "$(git ls-tree -r --name-only $t -- $m/src/main | grep -c '\.java$')" \
      "$(git ls-tree -r --name-only $t -- $m/src/main | grep -vc '\.java$')"
  done
done

# The Java EE / Jakarta EE check that produced "no difference"
git grep -h -E '^import (javax|jakarta)\.' 0.1.16 -- '*/src/*' | sort | uniq -c
git grep -h -E '^import (javax|jakarta)\.' 0.2.16 -- '*/src/*' | sort | uniq -c

# The commits behind the retained differences
git log -1 9c2cd7a   # Use Map.computeIfAbsent in Plugins
git log -1 afddc0e   # Add spring.factories for MyBatis auto-configuration (0.1.x)
git log -1 61ff309   # Add spring.factories for MyBatis Cloud auto-config (0.1.x)
git log -1 ebfe83c   # Use pattern matching for instanceof in interceptor (excluded)
```

## Related documents

- [`test-diff-0.2.16-vs-0.1.16.md`](./test-diff-0.2.16-vs-0.1.16.md) — the `src/test` counterpart
- [Documentation home](../../README.md)
- [User Guide](../../user-guide/README.md) · [Developer Guide](../../developer-guide/README.md)
- Module-level source analysis:
  [core](../../developer-guide/current/core.md) ·
  [spring-boot](../../developer-guide/current/spring-boot.md) ·
  [spring-cloud](../../developer-guide/current/spring-cloud.md) ·
  [test](../../developer-guide/current/test.md)

## License

This document is released under the [Apache License, Version 2.0](../../../LICENSE).
