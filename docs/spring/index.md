# Spring Integration

Litebridge provides first-class support for Spring and Spring Boot, allowing easy integration into applications.

## Supported Versions

Litebridge is tested against Spring Boot 3/Framework 6 and Spring Boot 4/Framework 7.
Since the ORM itself is compiled against Java 21, the earliest supported Spring Boot version is Spring Boot 3.2.0:

| Spring Boot | Spring Framework |
|:------------|:-----------------|
| 3.2.0       | 6.1.1            |
| 4.1.0       | 7.0.8            |

## Integration Options

There are two primary ways to integrate Litebridge with Spring:

1.  **[Spring Boot Starter](spring-boot-starter.md)**: The recommended approach for Spring Boot applications. It provides autoconfiguration for `Litebridge` and `LitebridgeTransactionManager` beans.
2.  **[Manual Configuration](manual-configuration.md)**: For non-Boot Spring applications or when full control over bean instantiation is required.

## Core Components

The following components are provided by the `litebridge-spring` module:

- `LitebridgeTransactionManager`: A Spring `PlatformTransactionManager` implementation that integrates Litebridge with Spring's `@Transactional` support.
- `Litebridge`: The main entry point, which can be easily defined as a Spring bean.
