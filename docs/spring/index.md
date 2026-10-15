# Spring Integration

Litebridge provides first-class support for Spring and Spring Boot, allowing easy integration into applications.

## Supported Versions

Litebridge is tested against Spring Boot 3/Framework 6 and Spring Boot 4/Framework 7.
Since the ORM itself is compiled against Java 21, the earliest supported Spring Boot version is Spring Boot 3.2.0:

| Spring Boot | Spring Framework |
|:------------|:-----------------|
| 3.2.0       | 6.1.1            |
| 4.1.1       | 7.0.9            |

## Integration Options

There are two primary ways to integrate Litebridge with Spring:

1.  **[Spring Boot Starter](spring-boot-starter.md)**: The recommended approach for Spring Boot applications. It provides autoconfiguration for `Litebridge` and `LitebridgeTransactionManager` beans, and Spring Data repositories.
2.  **[Manual Configuration](manual-configuration.md)**: For non-Boot Spring applications or when full control over bean instantiation is required.

## Core Modules & Components

Litebridge Spring integration is organised across dedicated modules:

- `litebridge-spring-common`: Houses version-agnostic integration classes such as `LitebridgeEntityScanner` and `LitebridgeTransactionManager`.
- `litebridge-spring`: Native integration for Spring Framework 7 / Spring Boot 4 using Spring Data 4 APIs.
- `litebridge-spring6`: Native integration for Spring Framework 6 / Spring Boot 3 using Spring Data 3 APIs.
- `litebridge-spring-boot-starter`: Starter for Spring Boot 4 applications.
- `litebridge-spring-boot3-starter`: Starter for Spring Boot 3 applications.

The key components include:

- `LitebridgeTransactionManager`: A Spring `PlatformTransactionManager` implementation that integrates Litebridge with Spring's `@Transactional` support.
- `Litebridge`: The main entry point, which can be easily defined as a Spring bean.
- `LitebridgeRepository`: Base Spring Data repository interface extending `ListCrudRepository` and `ListPagingAndSortingRepository`.
- `@EnableLitebridgeRepositories`: Annotation enabling Spring Data repository support with custom configuration.
- `@Query`: Annotation for defining custom native SQL queries and mutating statements on repository methods.
- Custom repository implementations: Standard Spring Data repository fragment composition (`*Custom` / `*Impl`) using Litebridge's fluent query API.
