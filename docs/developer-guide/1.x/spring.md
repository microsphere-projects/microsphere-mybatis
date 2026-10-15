# microsphere-mybatis-spring (1.x) — Developer Guide

[中文](zh/spring.md) · [← 1.x Index](README.md) · [Current analysis](../current/spring.md)

**No source differences vs `current`.** The registrar family, annotation attribute →
BeanDefinition mapping, `"*"`/blank bean-reference conventions, and
`SqlSessionFactoryBeanPostProcessor` are identical — see
[current/spring.md](../current/spring.md) for the full analysis.

## Line context

| Aspect | 1.x |
|---|---|
| Spring generation | Spring Framework 5.x / mybatis-spring 2.1.2 (vs 6.x / 4.1.0) |
| API compatibility | the registrar targets `SqlSessionFactoryBean` and `Configuration` setters that exist in both mybatis-spring lines — hence zero source diffs |
| `proxyFactory` fix | the `Class`→instance fix shipped as 0.1.16, mirroring 0.2.15 on current |

The `@MyBatisConfiguration` attribute list maps to `Configuration` methods that are stable
from MyBatis 3.5.x through both lines, so the same ~34 attributes apply.

## See Also

- [1.x differences overview](README.md)
- [User Guide](../../user-guide/1.x/spring.md)
