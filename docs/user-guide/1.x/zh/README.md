# 用户文档 —— `1.x`（0.1.x，`release-1.x` 分支）

文档对应最新发布版本：**0.1.16** · Java 8+ · Spring Boot 2.x · Spring Cloud Hoxton – 2021.0.x
· MyBatis 3.5.19 · MyBatis-Spring 2.1.2 · mybatis-spring-boot-starter 2.1.2

English：[1.x README](../README.md) · 现代版本线：[current](../../current/zh/README.md)

## 1.x 与 `current`（0.2.x）的差异

| 维度 | `1.x`（0.1.x） | `current`（0.2.x） |
|---|---|---|
| Java 基线 | 8（CI 验证 8/11/17/21/25） | 17（CI 验证 17/21/25） |
| MyBatis-Spring | 2.1.2 | 4.1.0 |
| mybatis-spring-boot-starter | 2.1.2 | 3.0.5 |
| Spring Cloud | Hoxton – 2021.0.x | 2022.0.x – 2025.0.x |
| 上游 BOM | microsphere-spring-cloud 0.1.26 | 0.2.26 |
| Boot 自动配置注册 | `META-INF/spring.factories`（`EnableAutoConfiguration=` 条目） | `META-INF/spring/...AutoConfiguration.imports` |
| `MyBatisAutoConfiguration` 注解 | `@ConditionalOnMyBatisAvailable` + `@EnableMyBatisExtension` + `@AutoConfigureAfter` | 相同 |
| 源码差异 | 传统 `instanceof` 强转、`Plugins` 使用 `MapUtil.computeIfAbsent` | 模式匹配、`java.util.Map#computeIfAbsent` |

两条版本线的**公开 Java API 完全一致** —— `current` 目录中记录的每个类、方法与注解属性
在 0.1.x 中均以相同签名存在（所有类型均 `@since 1.0.0`）。因此本目录下的文档只给出
1.x 特有事实（坐标、版本、注册文件），完整 API 参考链接至 `current` 对应页面。

## 模块文档

1. [microsphere-mybatis-utils](utils.md)
2. [microsphere-mybatis-core](core.md)
3. [microsphere-mybatis-test](test.md)
4. [microsphere-mybatis-spring](spring.md)
5. [microsphere-mybatis-spring-test](spring-test.md)
6. [microsphere-mybatis-spring-boot](spring-boot.md)
7. [microsphere-mybatis-spring-cloud](spring-cloud.md)
8. [parent 与 dependencies](parent-and-dependencies.md)

## 快速开始

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-mybatis-dependencies</artifactId>
            <version>0.1.16</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

> **不要混用版本线。** Spring Boot 2.x / javax 应用必须使用 0.1.x 构件；
> 引入 0.2.x 会在运行期失败（缺少 `jakarta` 类、MyBatis-Spring 4 API 不兼容）。
