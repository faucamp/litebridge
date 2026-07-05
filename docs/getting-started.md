# Getting Started

← [Home](index.md)

This guide will help you set up Litebridge in your Java project.

## Requirements

- **Java 21+**: Litebridge leverages modern Java features like records and pattern matching.
- **Maven**: For dependency management.

## 1. Add Litebridge to your project

To use Litebridge, add the `litebridge-orm` dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.litebridgedb</groupId>
    <artifactId>litebridge-orm</artifactId>
    <version>0.3.0</version> <!-- Replace with latest version -->
</dependency>
```

## 2. Choose a Database Provider

Litebridge uses a modular architecture for database support. You must include a database provider artifact corresponding to your database.

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

If you are using Spring or Spring Boot, Litebridge provides dedicated integration modules.

### Spring Boot Starter

The easiest way to integrate Litebridge into a Spring Boot application is using the starter:

```xml
<dependency>
    <groupId>org.litebridgedb</groupId>
    <artifactId>litebridge-spring-boot-starter</artifactId>
    <version>0.3.0</version> <!-- Replace with latest version -->
</dependency>
```

This dependency includes `litebridge-orm`, but you still need to include the desired database provider.

For more details on Spring integration, see:
- [Spring Integration Overview](spring/index.md)
- [Spring Boot Starter Guide](spring/spring-boot-starter.md)
- [Manual Spring Configuration](spring/manual-configuration.md)

## Next Steps

Now that you have added the dependencies, you can start exploring the [Litebridge API](persistence/index.md).
