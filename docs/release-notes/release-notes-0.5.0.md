### Litebridge 0.5.0 Release Notes

## [0.5.0] - 2026-10-15

Litebridge 0.5.0 delivers major functional and architectural advancements, introducing comprehensive SQL `MERGE` 
and upsert capabilities, rich table and subquery aliasing, and an overhauled high-performance `DtoMapper` 
with mapping plan caching. 

This release brings first-class Spring Data repository integration with derived query methods and `@Query` support 
across both Spring Boot 3 and Spring Boot 4 runtimes. 

Additionally, a modularised Database Provider SPI and dynamic `LitebridgeBuilder` architecture allow database providers
to expose vendor-tailored APIs (such as Oracle `INSERT ALL`) while optimising query compilation and batch execution 
across all supported dialects.

### Added

- Commons:
    - `ConcurrentLazyFunction` class for lazy functional initialisation of values in a thread-safe manner.
    - `BooleanUtils.requireTrue` and `BooleanUtils.requireFalse` methods for asserting boolean conditions.
    - `ModuleUtils` extended to be more versatile with MethodHandles.Lookup instances.
- ORM:
    - `MERGE INTO` support: Implement `Litebridge.mergeInto()` method for performing SQL `MERGE` operations.
    - Implement `Litebridge.merge()` method for upserting entities/DTOs.
    - New `saveAll()` overloads in `Litebridge` class for persisting multiple DTOs.
    - New query-based `insert()` API in `Litebridge` class, returning `InsertResult`.
    - Return `UpdateResult` and `InsertResult` classes for mutating operations, providing details on rows affected and
      generated keys.
    - Add batch update operations for native SQL.
    - Add new DTO mapper via the `DtoMapper` class.
    - Add `MappingPlanCache` to increase performance for mapping structurally-similar results to entities/DTOs
      by caching the calculated mapping plans in `DtoMapper`.
    - Add `LitebridgeBuilder` for instantiating dynamic Litebridge instances with ease.
      This allows database providers to "inject" additional `Litebridge` APIs for database-specific functionality.
    - Add `LitebridgeOverrideDatabaseProvider` interface in new ORM SPI package. This allows the Database Provider 
      to specify what Litebridge APIs are available at compile time,
      thus making the `Litebridge` instance database-specific. This allows future expansion to expose unique database
      vendor capabilities via the main Litebridge API.
    - Selecting literal values are now fully supported; added `Fn.literal()` factory method.
    - Support added for select statements without from clauses.
    - Table, DTO, and subquery aliasing support in `from()` and `join()` clauses via `Fn.aliasTable()`, `Fn.alias()`,
      and `FromClauseStart.from(DtoAliasSpec)`.
    - Add `Fn.aliasRef()` helper methods for referencing aliased columns, tables, and subqueries in query conditions
      and clauses.
    - Add `Fn.exists()` helper method for referencing subqueries in query conditions and clauses.
    - Add `DtoWhereCriteriaBuilder` for programmatic and conditional `WHERE` clause construction.
    - Add top-level API shortcut `select()` methods to make selecting literal numbers/booleans easier; this helps with existence checks.
    - Query expression specs extended to allow them to alter the physical structure of the query (which allows for specific optimisations).
    - `Fn.exists()` added as a select expression spec, executing an efficient existence check for a query.
    - Add `LitebridgeCore.toDtos()` for mapping collections of database rows directly to DTOs.
    - Add `NativeSqlContext.parse()` and `ParsedSql.usesNamedBindParameters()` for native SQL inspection and parameter handling.
    - Add `LitebridgeInspector` accessors for database provider and table registry.
- Spring Integration:
    - Update to latest supported Spring Framework 7 and Spring Boot 4.1.x versions.
    - Add Spring Data repository integration (`LitebridgeRepository`) with `ListCrudRepository` and `ListPagingAndSortingRepository` support.
    - Add support for Spring Data Derived Query Methods via `LitebridgePartTreeQuery`.
    - Add `@Query` annotation support for custom native SQL queries and mutating statements in repositories via `LitebridgeStringQuery`.
    - Support positional and named parameter binding, entity and raw `Row` return types, scalar aggregations, and modifying queries in Spring Data repositories.
    - Add `@EnableLitebridgeRepositories` and auto-configuration for Spring Data repositories via `LitebridgeRepositoriesAutoConfiguration`.
    - Add `litebridge-spring-common` module containing the bulk of the version-agnostic Spring integration components.
    - Add `litebridge-spring6` module targeting Spring Framework 6 / Spring Boot 3 natively with Spring Data 3 APIs.
    - Add `litebridge-spring-boot3-starter` combining `litebridge-spring6` and `litebridge-spring-boot-autoconfigure`.
- Database Provider SPI
    - New APIs for retrieving database and database provider metadata.
    - New `executeBatch()` method for explicit batch update operations.
    - `Result` and `BatchUpdateResult` result models added.
    - `VirtualTable` class added to encapsulate SELECT sources other than tables.
    - Add `tableAlias()` method to `AliasedExpression` to retrieve parent or source table aliases.
    - Support added for SQL `CAST` function.
    - Support added for SQL `EXISTS` keyword.
- Oracle Database Provider:
    - Add custom mathematical operation SQL generator
    - Multi-row inserts are now supported via batched insert statements
    - Add custom `LitebridgeOracle` class for Oracle-specific Litebridge APIs
    - Add `INSERT ALL` support via `LitebridgeOracle.insertAll()`
- SQLite Database Provider:
    - Specify limited capabilities: Override the Litebridge instance created by `LitebridgeBuilder` to `LitebridgeCore`

### Changed

- Commons:
    - `ConcurrentLazy` API updated to be more natural; `optional()` becomes `get()`, and similar renames.
- ORM:
    - Refactor `QueryCompiler` to delegate specific query types to specialised sub-compilers.
    - Reimplement and optimise the query compilation process.
    - Simplify node compilation; drop `SelectSpec`, `InsertSpec` etc.
    - Standardised AST cache handling.
    - Optimise and greatly simplify fluent API implementation due to full shift to AST compiler.
    - Reimplement the default DTO mapper (now called `DtoMapper`) to be simpler and more performant and handle non-ORM
      generated query results better.
    - `Litebridge.save()` now accepts a single DTO instead of varargs to improve clarity and type safety.
    - `Litebridge.save()` now defaults to a `MERGE` operation (if available) to upsert entities/DTOs if their
      persistence state is unknown, and they do not require PK value generation.
    - Query-based `update()` and `delete()` APIs now return a result instead of `void`.
    - `UpdateResult` class hierarchy extended; update results can now be `BatchUpdateResult`.
    - `InsertResult` now supports multi-row insert operation results by default; this changes how generated keys are
      accessed.
    - Split the `Litebridge` class into `LitebridgeCore` (all methods except merge) and `Litebridge` (merge-capable).
    - Propagate select expression aliases across query compilation contexts, enabling direct and contextual alias
      referencing in `WHERE`, `GROUP BY`, `HAVING`, and `ORDER BY` clauses.
    - `SqlMergeUsingStep.using()` now accepts `FromTargetSpec`.
    - SQL mode now handles type override expressions the same way as DTO mode, resulting in a more user-friendly API. 
- Spring Integration:
    - Split `litebridge-spring` integration into dedicated modules: `litebridge-spring` for Spring Framework 7 / Spring Boot 4 and `litebridge-spring6` for Spring Framework 6 / Spring Boot 3.
    - Most of what was in `litebridge-spring` is now in `litebridge-spring-common`.
    - Update `litebridge-spring-boot-starter` to explicitly bundle Spring Boot 4 integration (`litebridge-spring`).
- Database Provider SPI:
    - Standardised and simplified the `DatabaseProvider` SPI interface. This breaks backward compatibility.
    - Massive refactor of `AbstractDatabaseProvider` to separate concerns make it more modular
    - Simplify `AbstractDatabaseProvider` configuration; remove boilerplate.
    - Split SQL generation and execution into distinct engine components for improved flexibility and maintainability.
    - Native SQL and ORM-generated SQL now follow the same execution path.
    - Mathematical operations can now be overridden more simply in database providers
    - `LabelGenerator` replaces `ColumnIdentifierGenerator`; simplify and standardise alias handling
    - Refactor and simplify `SelectExpression` reference implementations.
    - `Row` and `RowColumn` models re-created for better user experience and performance.
- PostgreSQL Database Provider:
    - Add postgres-specific `COUNT(*)` expression to handle references to it in conditions using expression functions correctly.
- Oracle Database Provider:
    - Improve aliasing behaviour by enforcing Oracle's SELECT clause processing order 
- Spring Boot starter:
    - The autoconfiguration class now uses a `FactoryBean` to create a database provider-specific Litebridge bean type (e.g. `LitebridgeOracle`)

### Fixed

- ORM:
    - MethodHandles.Lookup not being honoured in `DtoConstructor`
- Spring Integration:
    - Resolved Spring Data derived query lookup strategy compatibility across both Spring Boot 3.2.x and Spring Boot 4.x runtimes in `LitebridgeRepositoryFactory`.
    - Support cross-version property path extraction between Spring Data Commons 3.x and 4.x in `LitebridgePartTreeQuery`.
- Oracle Database Provider:
    - Fixed the modulus math operation
    - Fixed multi-row inserts; now uses batched insert statements 
- SQLite Database Provider:
    - Use correct `LIMIT -1` when a SELECT statement is generated with an offset but not limit
    
### Removed

- ORM:
    - `Litebridge.save(Object... dtos)` has been removed in favour of `saveAll()`.
    - `SelectSpecDtoMapper`: replaced by `DtoMapper`
    - Legacy query-building specifications: `SelectSpec`, `InsertSpec`, `UpdateSpec`, `DeleteSpec`
    - Removed `AliasGeneratorFactory`
    - Removed obsolete `SelectFieldSpec` query expression class.
- Database Provider SPI:
    - Legacy SPI interfaces have been removed, such as operation-specific execution methods (`select()`, `insert()`,
      `nativeQuery()`, etc.).
    - `ColumnIdentifierGenerator`; replaced by `LabelGenerator`.
    - Removed `ColumnValue`, `RowValue` etc due to alias handling improvements.
    - Removed obsolete `AliasTransformer` interface and default implementation (also removed it from Database Provider implementations).
