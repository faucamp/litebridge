# Agent Guidelines: Litebridge

This document provides essential context and guidelines for AI agents working on the Litebridge project.

## Project Overview

Litebridge is a fast, lightweight Object-Relational Mapper (ORM) for Java 21+. 

It focuses on a SQL-like fluent API and ease of use, which can be used in a entity/DTO mode (clasic ORM) or a "raw SQL"-mode.

It supports annotated entity classes as well as unannotated, plain DTOs as database entities.

## Project Structure

Litebridge is modular and uses JPMS (`module-info.java`).

- `docs`: Documentation and guides for users and contributors. It is contains subsections detailing various aspects, logically grouped.
- `litebridge-orm`: The core engine and primary entry point. Contains the `Litebridge` class.
- `litebridge-orm-support`: Supporting utilities for Litebridge ORM, allowing classpath scanning for entities/DTO-table mappings.
- `litebridge-db`: Contains the `DatabaseProvider` SPI and its implementations (e.g. `litebridge-db-h2`, `litebridge-db-postgres`, etc.).
- `litebridge-annotations`: Entity annotation definitions.
- `litebridge-tracking`: An independent change tracking API for arbitrary Java objects.
- `litebridge-converter`: Type conversion utilities for translating between Java and SQL types.
- `litebridge-commons`: Internal utility classes used across the project to minimize external dependencies.
- `spring/litebridge-spring`: Integration library for Spring projects.
- `spring/litebridge-spring-boot-autoconfigure`: Auto-configuration for Spring Boot applications.
- `spring/litebridge-spring-boot-starter`: Starter for Spring Boot applications to simplify setup.
- `litebridge-maven-plugin`: Maven plugin for reverse engineering database tables to Litebridge ORM entities, and entity metamodel creation. 
- `example`: Contains example applications demonstrating Litebridge usage.
- `web`: Contains the project's web site.

## Technical Stack

- **Java**: 21+ (Modular)
- **Build System**: Maven
- **Null Safety**: JSpecify (`@NullMarked`, `@Nullable`).
- **Access Control**: Uses `java.lang.invoke.MethodHandles.Lookup` for efficient DTO field/property access.
- **Testing**: JUnit 6, Mockito, Testcontainers.
- **Web site**: Astro, Tailwind

## Development Guidelines

### 1. Code Style

- Follow the existing project style: 4-space indentation, standard Java naming conventions.
- **Null Safety**: Always use JSpecify annotations on public APIs. The project is generally `@NullMarked`.
- **Modularity**: Respect module boundaries defined in `module-info.java`.
- **API Design**: Favour fluent builders and functional interfaces for configuration and queries.
- **Variables and parameters**: Declare variables and parameters as `final` wherever possible, including when writing tests.

### 2. Entity/DTO Mappings

- Litebridge supports unaltered DTOS as well as annotated entity classes as database entities.
- Entity classes are annotated with `org.litebridge.orm.annotation.Table`
- Unannotated DTOs can be used via the fluent programmatic registration API.
- All entity/DTO mappings are registered via `Litebridge.register(...)` methods.

### 3. How Litebridge Works

- The `Litebridge` class exposes various methods for querying and updating data.
- There are two main "modes" of operation: DTO/entity-based, and SQL-based. 
In DTO mode, the API refers to class fields in expressions and returns a mapped DTO (or other type, dependent on the query). 
In SQL-mode, "raw" row data is returned. The API changes this based on what is provided as input for the various steps in the API chain, notably the `from()` part.
- The API has evolved from simple column-based access to query expressions. Query power what SQL is generated in different clauses. The API is designed to support simple string-based parameter specification and query expressions.
- Query expressions are the the primary component of the query API. They represent SQL functions, columns, literal expressions and specialised Java-side conversions. They are typically created using static methods in the `org.litebridge.orm.expression.Fn` utility class, or through metamodel fields. 
- There are 3 mains phases of query expressions:
  - Proto-query expressions: in the first few steps of the fluent API, there is potentially not enough information to determine e.g. a target table
  - Expression specifications: Non-ambigous expression of intent (e.g. selecting a specific column in a specific table)
  - Select Expressions: The final expression as provided by the database provider. Capable of rendering SQL and used in generation of SQL statements.
- Metatmodels of entities/DTOs provided static query expressions with the same name as the entity/DTO's fields, and enable type-safe queries. They can be created via the Maven plugin or hand-crafted.

### 3. Database Support

- Database-specific logic (SQL dialect, metadata handling) must reside in `DatabaseProvider` implementations.
- New database support should be added as a new module in the `litebridge-db` directory.
- The `AbstractDatabaseProvider` class in module `litebridge-db-spi-impl provides a starting point for implementing the
  SPI, but is not strictly required. It can also be modified to accommodate specific database requirements if needed.

### 4. Testing

- **E2E Tests**: Found in `litebridge-orm/src/test/java/.../e2e/`. Use these for verifying full feature integration. 
They are bound to Maven's `integration-test` phase and thus executed using `mvn verify` by default.
- **Database Environments**: Use the `MultiDbTestExtension` to run tests against multiple database providers (H2,
  Oracle, etc.). These can be set via command line/Maven by using the `lb.e2e.env` property. Valid values are:
    - `all` - Run against all supported databases (this is the default no `lb.e2e.env` property is provided)
    - `h2` - Run against an in-memory H2 database
    - `oracle` - Run against Oracle XE via testcontainers
    - `postgres` - Run against a PostgreSQL database via testcontainers
    - `sqlite` - Run against an in-memory SQLite database
    - `none` - Disable E2E integration tests. This is useful when making targeted changes that need quick testing.
- **Mocking**: Use Mockito for unit tests that don't require a live database.
- **Style**: Use JUnit 6 conventions for test classes and methods, and use the existing "Given-When-Then" pattern for
  test setup where possible (E2E tests are mostly exempt from this). Use `// Given`, `// When` and `// Then` comments to
  document test steps when following this pattern.
- **Coverage**: Aim for 100% test coverage in unit tests, and that E2E tests cover the majority of use cases.

## Key Classes and APIs

- `org.litebridge.orm.Litebridge`: The main entry point for `save`, `select`, `update`, `delete`.
- `org.litebridge.db.spi.DatabaseProvider`: The SPI that must be implemented for each supported database.

## Common Agent Tasks

- **Write Documentation**: Follow the same style as existing documentation. Use a formal tone; avoid using words like "you". Documentation is found in the `docs`
  directory. Ensure that all relevant pages are updated when adding/extending a specific topic.
- **Adding a DB Provider**: Implement the `DatabaseProvider` SPI in a new module and ensure it passes the SPI TCK/common
  tests. Update relevant documentation to reflect the new provider. Add unit tests and E2E tests for the new provider.
- **Creating tests**: Implement unit tests for new features or bug fixes. Follow the style detailed under section 4, "Testing".
