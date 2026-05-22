# Agent Guidelines: Litebridge

This document provides essential context and guidelines for AI agents working on the Litebridge project.

## Project Overview

Litebridge is a fast, lightweight Object-Relational Mapper (ORM) for Java 21+. It focuses on a "SQL-first" philosophy,
minimizing magic and favoring programmatic configuration over heavy abstraction or annotations.

## Core Philosophy

- **Modern Java**: Requires Java 21+. Leverage modern features like records, pattern matching, and the Java Platform
  Module System (JPMS).
- **Minimal Magic**: Avoid complex toolchains, code generation, or heavy annotation processing. It is designed to use
  unmodified DTOs as entities.
- **SQL-First**: SQL is treated as a first-class citizen. Queries and mappings are primarily defined using a fluent API.
- **Performance**: High performance with minimal database round-trips via built-in change tracking.

## Project Structure

Litebridge is modular and uses JPMS (`module-info.java`).

- `litebridge-orm`: The core engine and primary entry point. Contains the `Litebridge` class.
- `litebridge-db`: Contains the `DatabaseProvider` SPI and its implementations (e.g. `litebridge-db-h2`).
- `litebridge-tracking`: An independent change tracking API for arbitrary Java objects.
- `litebridge-converter`: Type conversion utilities for translating between Java and SQL types.
- `litebridge-commons`: Internal utility classes used across the project to minimize external dependencies.
- `spring/litebridge-spring`: Integration library for Spring projects.
- `spring/litebridge-spring-boot-autoconfigure`: Auto-configuration for Spring Boot applications.
- `spring/litebridge-spring-boot-starter`: Starter for Spring Boot applications to simplify setup.
- `docs`: Documentation and guides for users and contributors.

## Technical Stack

- **Java**: 21+ (Modular)
- **Build System**: Maven
- **Null Safety**: JSpecify (`@NullMarked`, `@Nullable`).
- **Access Control**: Uses `java.lang.invoke.MethodHandles.Lookup` for efficient DTO field/property access.
- **Testing**: JUnit 6, Mockito, Testcontainers.

## Development Guidelines

### 1. Code Style

- Follow the existing project style: 4-space indentation, standard Java naming conventions.
- **Null Safety**: Always use JSpecify annotations on public APIs. The project is generally `@NullMarked`.
- **Modularity**: Respect module boundaries defined in `module-info.java`.
- **API Design**: Favour fluent builders and functional interfaces for configuration and queries.
- **Variables and parameters**: Declare variables and parameters as `final` wherever possible.

### 2. DTO Mappings

- Mappings are defined via `TableSpec`.
- Prefer programmatic registration (`litebridge.register(...)`) over annotations.

### 3. Database Support

- Database-specific logic (SQL dialect, metadata handling) must reside in `DatabaseProvider` implementations.
- New database support should be added as a new module in the `litebridge-db` directory.
- The `AbstractDatabaseProvider` class in module `litebridge-db-spi-impl provides a starting point for implementing the
  SPI, but is not strictly required. It can also be modified to accommodate specific database requirements if needed.

### 4. Testing

- **E2E Tests**: Found in `litebridge-orm/src/test/java/.../e2e/`. Use these for verifying full feature integration.
- **Database Environments**: Use the `MultiDbTestExtension` to run tests against multiple database providers (H2,
  Oracle, etc.). These can be set via command line/Maven by using the `lb.e2e.env` property. Valid values are:
    - `all` - Run against all supported databases (this is the default no `lb.e2e.env` property is provided)
    - `h2` - Run against an in-memory H2 database
    - `sqlite` - Run against an in-memory SQLite database
    - `oracle` - Run against Oracle XE via testcontainers
    - `none` - Disable E2E integration tests. This is useful when making targeted changes that need quick testing.
- **Mocking**: Use Mockito for unit tests that don't require a live database.
- **Style**: Use JUnit 6 conventions for test classes and methods, and use the existing "Given-When-Then" pattern for
  test setup where possible (E2E tests are mostly exempt from this). Use `// Given`, `// When` and `// Then` comments to
  document test steps when following this pattern..
- **Coverage**: Aim for 100% test coverage in unit tests, and that E2E tests cover the majority of use cases.

## Key Classes and APIs

- `org.litebridgedb.orm.Litebridge`: The main entry point for `save`, `select`, `update`, `delete`.
- `org.litebridgedb.orm.api.spec.TableSpec`: Core class for defining DTO-to-table mapping.
- `org.litebridgedb.db.spi.DatabaseProvider`: The SPI that must be implemented for each supported database.
- `org.litebridgedb.tracking.ChangeTracker`: Handles state tracking for DTOs to optimize updates.

## Common Agent Tasks

- **Write Documentation**: Follow the same style as existing documentation. Documentation is found in the `docs`
  directory.
- **Fixing a Bug**: Create a reproduction test case in the `e2e` package of `litebridge-orm`.
- **Adding a Feature**: Start by defining the API in `Litebridge` or relevant spec classes, then implement the logic in
  `litebridge-orm`. Add E2E tests.
- **Adding a DB Provider**: Implement the `DatabaseProvider` SPI in a new module and ensure it passes the SPI TCK/common
  tests. Update relevant documentation to reflect the new provider.
