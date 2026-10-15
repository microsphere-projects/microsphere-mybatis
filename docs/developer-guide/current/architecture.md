# Architecture Design — Microsphere MyBatis 0.2.x (`current`)

[中文](zh/architecture.md) · [← Developer Guide Index](../README.md) · [1.x](../1.x/architecture.md)

---

## Project Positioning

Microsphere MyBatis sits **between** the MyBatis framework and the Spring container.
It does not replace MyBatis — it adds an observable, chainable extension layer on top of
MyBatis' `Executor` interface, then integrates that layer into Spring's bean lifecycle.

```
┌──────────────────────────────────────────────────────────┐
│  Application (your code)                                 │
│  @SpringBootApplication  @Bean ExecutorFilter            │
├──────────────────────────────────────────────────────────┤
│  microsphere-mybatis-spring-cloud   (HasFeatures)        │
│  microsphere-mybatis-spring-boot    (AutoConfiguration)  │
│  microsphere-mybatis-spring         (@EnableMyBatis…)    │
├──────────────────────────────────────────────────────────┤
│  microsphere-mybatis-core         ExecutorFilter/Interceptor chain  │
│  microsphere-mybatis-utils        Bootstrap & template APIs         │
│  microsphere-mybatis-test         JUnit 5 extension & fixtures      │
│  microsphere-mybatis-spring-test  Spring fixture configs             │
├──────────────────────────────────────────────────────────┤
│  MyBatis 3.5.x   +   mybatis-spring 4.1.0   (optional deps)│
└──────────────────────────────────────────────────────────┘
```

---

## Module Dependency Graph (compile-time, non-optional edges)

```mermaid
graph TD
    subgraph build ["Build Infrastructure (pom-only)"]
        PARENT["microsphere-mybatis-parent\n(pom, inherits microsphere-spring-cloud-parent)"]
        DEPS["microsphere-mybatis-dependencies\n(published BOM)"]
    end

    subgraph core ["Foundation"]
        UTILS["microsphere-mybatis-utils\nMyBatisUtils"]
        CORE["microsphere-mybatis-core\nExecutorFilter chain\nExecutorInterceptor chain\nInterceptingExecutorInterceptor"]
    end

    subgraph test_layer ["Test Infrastructure"]
        TEST["microsphere-mybatis-test\n@MyBatisTest extension\nAbstract test hierarchy\nH2 fixture resources"]
        SPRING_TEST["microsphere-mybatis-spring-test\nSpring fixture configs\nDB lifecycle"]
    end

    subgraph spring_layer ["Spring Integration"]
        SPRING["microsphere-mybatis-spring\n@EnableMyBatis\n@MyBatisConfiguration\n@EnableMyBatisExtension"]
    end

    subgraph boot_layer ["Spring Boot / Cloud"]
        BOOT["microsphere-mybatis-spring-boot\nMyBatisAutoConfiguration\nConditionalOnMyBatis*"]
        CLOUD["microsphere-mybatis-spring-cloud\nMyBatisCloudAutoConfiguration\nfeatures.yaml"]
    end

    UTILS --> CORE
    UTILS --> TEST
    CORE --> SPRING
    TEST --> SPRING_TEST
    SPRING --> BOOT
    BOOT --> CLOUD

    CORE -.->|test scope| TEST
    SPRING -.->|test scope| SPRING_TEST
    BOOT -.->|test scope| SPRING_TEST
    CLOUD -.->|test scope| SPRING_TEST

    PARENT -.->|inherits| UTILS
    PARENT -.->|inherits| CORE
    PARENT -.->|inherits| TEST
    PARENT -.->|inherits| SPRING
    PARENT -.->|inherits| SPRING_TEST
    PARENT -.->|inherits| BOOT
    PARENT -.->|inherits| CLOUD
    DEPS -.->|BOM manages| CORE
    DEPS -.->|BOM manages| TEST
    DEPS -.->|BOM manages| SPRING
    DEPS -.->|BOM manages| SPRING_TEST
    DEPS -.->|BOM manages| BOOT
    DEPS -.->|BOM manages| CLOUD
```

Solid arrows = required compile-time dependency.  
Dotted arrows = optional / test-scope / build-time.

---

## External Dependency Strategy

All third-party and upstream-microsphere dependencies are declared **`optional`** in each module's
`pom.xml`. This means:

- The consumer's own BOM (`spring-boot-starter-parent`, `spring-cloud-dependencies`, etc.)
  controls the actual resolved versions.
- Microsphere MyBatis avoids version lock-in and can serve multiple Spring generations.
- The only *hard* compile-time requirement is internal cross-module edges
  (e.g. `core → utils`, `spring → core`).

The `microsphere-mybatis-parent` pom centralises the version properties (`mybatis.version`,
`mybatis-spring.version`, etc.) so that CI builds with different Spring Cloud profiles
(`spring-cloud-2022`, `spring-cloud-2023`, …) only need to activate a profile.

---

## End-to-End Execution Flow (Spring Boot application)

```mermaid
sequenceDiagram
    participant App as Application
    participant Boot as MyBatisAutoConfiguration
    participant Registrar as MyBatisExtensionBeanDefinitionRegistrar
    participant BPP as SqlSessionFactoryBeanPostProcessor
    participant SFB as SqlSessionFactoryBean
    participant MyBatis as MyBatis Configuration
    participant IEI as InterceptingExecutorInterceptor
    participant Chain as ExecutorFilterChain

    App->>Boot: Spring context starts
    Boot->>Registrar: @Import registers bean definitions
    Registrar->>Registrar: Discovers ExecutorFilter/Interceptor beans
    Registrar->>Registrar: Registers interceptingExecutorInterceptor + SqlSessionFactoryBeanPostProcessor
    BPP->>SFB: processBeforeInitialization
    BPP->>SFB: addPlugins(interceptingExecutorInterceptor)
    SFB->>MyBatis: afterPropertiesSet → builds SqlSessionFactory
    App->>MyBatis: sqlSession.update(ms, param)
    MyBatis->>IEI: plugin(Executor) → returns InterceptingExecutor
    IEI->>Chain: buildChain()
    Chain->>Chain: position 0 → filter1.update(...)
    Chain->>Chain: position 1 → adapter.update(...)
    Chain->>MyBatis: position N → delegate.update(...)
    MyBatis-->>Chain: SQLException? captured
    Chain-->>IEI: result propagated up
    IEI-->>App: rows affected
```

---

## Design Principles

1. **Non-intrusive extension** — the MyBatis plugin mechanism (`Interceptor.plugin(target)`) is
   used as the sole integration hook; no MyBatis or mybatis-spring class is subclassed or patched.
2. **Chain over proxy** — `ExecutorFilterChain` replaces MyBatis' single-proxy `Plugin` model with
   an explicit, composable chain; multiple `Interceptor` plugins can co-exist and merge.
3. **Observability without side effects** — `ExecutorInterceptor` callbacks are fire-and-forget:
   `InterceptorsExecutorFilterAdapter.iterate()` catches every `Throwable` and logs it at `warn`,
   guaranteeing that a broken audit/metrics callback cannot break the SQL path.
4. **Prioritized SPI** — both `ExecutorFilter` and `ExecutorInterceptor` extend
   `io.microsphere.lang.Prioritized`; the sort is stable and ascending.
5. **Test-first fixtures** — the test module ships its H2 config, mapper XMLs, init/destroy scripts
   as `src/main/resources` so that downstream modules and consumer tests reuse them without any
   `src/test/resources` setup.
6. **Version-line branching** — the Java-8 (`release-1.x`) and Java-17 (`release`/`main`) branches
   share the same design; only language features and auto-configuration registration files differ.

---

## Module Documentation Index

| Module | Developer Guide |
|---|---|
| [microsphere-mybatis-utils](utils.md) | Bootstrap utility class, `ThrowableConsumer` templates |
| [microsphere-mybatis-core](core.md) | Filter chain, adapter, `CachingExecutor` unwrapping |
| [microsphere-mybatis-test](test.md) | JUnit 5 extension lifecycle, resolver dependency graph |
| [microsphere-mybatis-spring](spring.md) | Registrar family, `SqlSessionFactoryBeanPostProcessor` |
| [microsphere-mybatis-spring-test](spring-test.md) | Event-driven database lifecycle |
| [microsphere-mybatis-spring-boot](spring-boot.md) | Auto-configuration ordering & conditions |
| [microsphere-mybatis-spring-cloud](spring-cloud.md) | `features.yaml` binding, `HasFeatures` beans |
| [parent & dependencies](parent-and-dependencies.md) | POM hierarchy, `${revision}` flatten, BOM scope |
