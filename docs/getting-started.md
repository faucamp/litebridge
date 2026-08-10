# Getting Started

← [Home](index.md)

This guide provides instructions for setting up Litebridge in a Java project.

## Requirements

- **Java 21+**: Litebridge leverages modern Java features like records and pattern matching.
- **Maven**: For dependency management.

## 1. Add Litebridge to the project

To use Litebridge, add the `litebridge-orm` dependency to the `pom.xml`:

```xml

<dependency>
    <groupId>org.litebridge</groupId>
    <artifactId>litebridge-orm</artifactId>
    <version>0.4.0</version> <!-- Replace with latest version -->
</dependency>
```

## 2. Choose a Database Provider

Litebridge uses a modular architecture for database support. A database provider artifact corresponding to the database must be included.

### Supported Database Providers

| Database       | Artifact ID              |
|----------------|--------------------------|
| **H2**         | `litebridge-db-h2`       |
| **Oracle**     | `litebridge-db-oracle`   |
| **PostgreSQL** | `litebridge-db-postgres` |
| **SQLite**     | `litebridge-db-sqlite`   |

Example for H2:

```xml

<dependency>
    <groupId>org.litebridge.db</groupId>
    <artifactId>litebridge-db-h2</artifactId>
    <version>0.4.0</version> <!-- Replace with latest version -->
</dependency>
```

## 3. Litebridge Maven Plugin (Optional)

The Litebridge Maven Plugin can automate the creation of entity classes from an existing database and generate metamodels for type-safe queries.

```xml

<plugin>
    <groupId>org.litebridge.maven</groupId>
    <artifactId>litebridge-maven-plugin</artifactId>
    <version>0.4.0</version> <!-- Replace with latest version -->
</plugin>
```

For more details, see the [Maven Plugin documentation](maven/index.md).

## 4. Spring Integration (Optional)

For applications using Spring or Spring Boot, Litebridge provides dedicated integration modules.

### Spring Boot Starter

The easiest way to integrate Litebridge into a Spring Boot application is using the starter:

```xml

<dependency>
    <groupId>org.litebridge</groupId>
    <artifactId>litebridge-spring-boot-starter</artifactId>
    <version>0.4.0</version> <!-- Replace with latest version -->
</dependency>
```

This dependency includes `litebridge-orm`, but the desired database provider must still be included.

For more details on Spring integration, see:
- [Spring Integration Overview](spring/index.md)
- [Spring Boot Starter Guide](spring/spring-boot-starter.md)
- [Manual Spring Configuration](spring/manual-configuration.md)

## 5. Configuration

Litebridge can be configured using `LitebridgeConfig` to alter global behaviour, such as how related DTOs are handled.

```java
LitebridgeConfig config = new LitebridgeConfig();
config.setRelatedDtoStrategy(RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN);

Litebridge litebridge = new Litebridge(databaseProvider, dataSource, config);
```

See the [Configuration Guide](persistence/configuration.md) for more details.

## Next Steps

After adding the dependencies and configuring Litebridge, the [Litebridge API](persistence/index.md) can be explored.
