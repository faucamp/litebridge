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
    <groupId>org.litebridgedb</groupId>
    <artifactId>litebridge-orm</artifactId>
    <version>0.3.0</version> <!-- Replace with latest version -->
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
    <groupId>org.litebridgedb</groupId>
    <artifactId>litebridge-db-h2</artifactId>
    <version>0.3.0</version> <!-- Replace with latest version -->
</dependency>
```

## 3. Litebridge Maven Plugin (Optional)

The Litebridge Maven Plugin can automate the creation of entity classes from an existing database and generate metamodels for type-safe queries.

```xml
<plugin>
    <groupId>org.litebridgedb.maven</groupId>
    <artifactId>litebridge-maven-plugin</artifactId>
    <version>0.3.0</version> <!-- Replace with latest version -->
</plugin>
```

For more details, see the [Maven Plugin documentation](maven/index.md).

## 4. Spring Integration (Optional)

For applications using Spring or Spring Boot, Litebridge provides dedicated integration modules.

### Spring Boot Starter

The easiest way to integrate Litebridge into a Spring Boot application is using the starter:

```xml
<dependency>
    <groupId>org.litebridgedb</groupId>
    <artifactId>litebridge-spring-boot-starter</artifactId>
    <version>0.3.0</version> <!-- Replace with latest version -->
</dependency>
```

This dependency includes `litebridge-orm`, but the desired database provider must still be included.

For more details on Spring integration, see:
- [Spring Integration Overview](spring/index.md)
- [Spring Boot Starter Guide](spring/spring-boot-starter.md)
- [Manual Spring Configuration](spring/manual-configuration.md)

## Next Steps

After adding the dependencies, the [Litebridge API](persistence/index.md) can be explored.
