# microsphere-mybatis-spring-cloud (1.x) — Developer Guide

[中文](zh/spring-cloud.md) · [← 1.x Index](README.md) · [Current analysis](../current/spring-cloud.md)

Same single empty `MyBatisCloudAutoConfiguration` (`@ConditionalOnMyBatisAvailable` +
`@AutoConfigureAfter(name=…)`), same `META-INF/config/default/features.yaml` (identical
content on both branches — four `abstract` groups plus the `named` entry; see
[current/spring-cloud.md](../current/spring-cloud.md)). Only discovery registration differs.

## Registration diff

As in the boot module, 1.x ships both files:

```
META-INF/spring.factories
    org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
    io.microsphere.mybatis.spring.cloud.autoconfigure.MyBatisCloudAutoConfiguration

META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
    io.microsphere.mybatis.spring.cloud.autoconfigure.MyBatisCloudAutoConfiguration
```

So the `features.yaml` → `HasFeatures` binding works across Spring Cloud
Hoxton – 2021.0.x (the 0.1.26 microsphere-spring-cloud generation supplies the same
features loader used on current).

## POM quirk (same on both lines)

`microsphere-mybatis-spring-test` is declared **without `<scope>test</scope>`** here as
well — it leaks as a compile-scope transitive dependency on `release-1.x` too. Consumers
on either line should exclude it.

## See Also

- [spring-boot 1.x](spring-boot.md) — identical dual-registration rationale
- [1.x differences overview](README.md)
- [User Guide](../../user-guide/1.x/spring-cloud.md)
