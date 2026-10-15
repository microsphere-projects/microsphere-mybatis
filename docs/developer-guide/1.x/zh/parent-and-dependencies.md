# Parent 与 Dependencies POM（1.x）—— 开发者文档

[English](../parent-and-dependencies.md) · [← 1.x 索引](README.md) · [current 源码分析](../../current/zh/parent-and-dependencies.md)

相同的三 POM 结构（根聚合 POM → `microsphere-mybatis-parent` →
`microsphere-mybatis-dependencies` BOM）、相同的 `${revision}` + flatten 方案、相同的
optional 依赖策略、相同的六构件 BOM 范围（依旧缺 utils）。
版本代际与 CI 矩阵发生变化。继承关系图与设计动机见
[current/parent-and-dependencies.md](../../current/zh/parent-and-dependencies.md)。

## 版本对照表（`release-1.x`）

| 项目 | 1.x | current |
|---|---|---|
| 根 POM 父级 | `microsphere-spring-cloud-parent` **0.1.26** | 0.2.26 |
| `revision` | `0.1.17-SNAPSHOT` | `0.2.17-SNAPSHOT` |
| `mybatis.version` | 3.5.19 | 3.5.19 |
| `mybatis-spring.version` | **2.1.2**（javax 时代，Spring 5） | 4.1.0（jakarta 时代，Spring 6） |
| `mybatis-spring-boot.version`（starter） | **2.1.2** | 3.0.5 |
| `cglib.version` | 3.3.0 | 3.3.0 |
| `microsphere-spring-cloud.version`（BOM 导入） | 0.1.26 | 0.2.26 |
| Java 基线 | 8 —— 1.x 根 POM **没有** `java.version` 属性，编译目标来自上游 0.1.26 父 POM | 17（显式 `java.version` 属性） |

## CI 矩阵差异

| | 1.x | current |
|---|---|---|
| `java` | 8、11、17、21、25 | 17、21、25 |
| `maven-profile-spring-cloud` | hoxton、2020、2021 | 2022、2023、2024、2025 |
| 组合数 | 15 | 12 |

两条线的工作流都以 `mvn --activate-profiles test,coverage,<profile> -Drevision=0.0.1-SNAPSHOT`
运行，所以对外兼容声明（0.1.x 支持 Java 8+ 上的 Spring Cloud Hoxton – 2021.0.x）的依据
是矩阵本身，而非文字。

## 另见

- [1.x 差异总览](README.md)
- [用户文档](../../../user-guide/1.x/zh/parent-and-dependencies.md)
