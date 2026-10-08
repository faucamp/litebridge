# Changelog

## [Unreleased]

### Added

- Commons:
    - `ConcurrentLazyFunction` class for lazy functional initialisation of values in a thread-safe manner.
    - `BooleanUtils.requireTrue` and `BooleanUtils.requireFalse` methods for asserting boolean conditions.
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

## [0.4.0] - 2026-08-10

### Added

- ORM:
    - New internal Abstract Syntax Tree (AST) for query representation (`QueryNode`, `SelectNode`, etc.), providing a
      robust foundation for query compilation and execution.
    - `QueryCompiler` for translating the AST into executable database specifications.
    - `QueryPlanCache` to cache and reuse compiled query plans, significantly improving performance for repeated
      queries.
    - Centralised alias management via `AliasGenerator` and `AliasGeneratorFactory`.
    - Robust support for nested conditions and complex `HAVING` clauses through `ConditionGroupNode` and `HavingNode`.
    - Introduced `TableMetaDataCache` for efficient retrieval and caching of database metadata.
    - Added `NativeSqlCache` for caching parsed named parameter-based queries.
- Database Provider SPI:
    - Introduced `PreparedOperation` to represent generic executable database operations.
    - Introduced `BindValueExpression` to represent bind parameters within the expression API.
    - `ColumnMetaData` now contains the default value of a column.
    - `Row` now supports index-based value retrieval for improved performance.
- Maven plugin:
    - Add `resolveRelationships` config parameter to control foreign key behaviour.
    - Add `initDefaultValues` config parameter to initialise default values for fields representing columns with a
      default value defined in the database.
    - Add `primitiveNotNulls` config parameter to control whether primitive fields are generated if the database column
      is not nullable, if applicable.
    - Add `generateConstructors` config parameter to control whether constructors are generated for entities.

### Changed

- ORM:
    - Refactored `select()`, `update()`, `delete()`, and `insert()` fluent APIs to use the new AST-based engine.
    - Re-engineered `PersistenceFacade` to integrate with the query plan cache and compilation pipeline.
    - Optimised relationship path resolution and implicit join generation during query compilation.
    - Significantly optimised DTO mapping performance in `SelectSpecDtoMapper` through a multi-layered approach:
        - Introduced a "compiled" `MappingPlan` to pre-calculate mapping logic, enabling index-based row access ($O (1)$
          column lookups).
        - Transitioned to `MethodHandle`-based DTO construction and field population to bypass standard reflection
          overhead.
        - Implemented a two-phase resolution process (Populate & Batch Resolve) to reduce mapping complexity
          from $O (N^2)$ to $O (N)$ for large result sets.
        - Added a high-performance DTO list cache to accelerate collection and relationship mappings.
- Maven plugin:
    - Refactor `ReverseEngineerMojo` to improve performance
    - Initialise default values for fields representing columns with a default value defined in the database.
    - Use `joinUsing` instead of `joinOn` annotation parameter if applicable.
- Database Provider SPI:
    - Standardised `DatabaseProvider` methods to use `PreparedSql` for statement execution.
- Type converter:
    - Boolean converter now supports strings "1" and "0" as valid boolean values.

### Deprecated

- Database Provider SPI:
    - Deprecated SPI method: `ColumnMetadata.setAutoIncrement()`

### Fixed

- ORM:
    - Fix database connection leak in the newly introduced `TableMetaDataCache`.
    - Correct behavior of nested conditions and `HAVING` clauses within the AST engine.
    - Fix setting generated PK value not being set if the target field is a primitive type.
- Commons:
    - Improve detection of basic Java types in `ClassUtils`.
- Maven plugin:
    - Fix issue with complex circular dependencies in the Litebridge Maven plugin's `reverse-engineer` goal.
    - Fix many-to-many join table indentification for tables with two columns (but aren't join tables).
    - Fix reverse collection `mappedByField` value if the target's field was altered because of "ID" suffix removal.
    - Fix `joinOn` annotation parameter not being set if no `columnMapping` config was provided.
- Spring:
    - Fix regression with automatic creation of `Litebridge` bean where it was being returned as `SelectApi`.
    - Fix transaction manager not releasing connections correctly.

## [0.3.1] - 2026-07-13

### Changed

- Renamed main `org.litebridgedb` package back to original `org.litebridge`
- JavaDocs expanded

## [0.3.0] - 2026-07-11

### Added

- ORM:
    - Advanced expression-based Query API supporting SQL functions (AVG, COUNT, UPPER, SUBSTRING, etc.).
    - New `select().from(...)` general-form query syntax for partial DTO population and custom projections.
    - Support for `GROUP BY` and `HAVING` clauses in fluent queries.
    - Type-safe query metamodels for compile-time validation.
    - Named bind parameters in Native SQL queries.
    - New query operators: `LIKE`, `NOT IN`.
    - Enhanced data type support (BLOB/CLOB, improved date/time conversion).
- Maven plugin:
    - Create Litebridge Maven Plugin for reverse-engineering schemas and generating type-safe metamodels.
    - Full JSpecify null-safety annotation support (@NullMarked, @Nullable).
- Spring:
    - Automatic Spring Boot entity registration via classpath scanning.

### Changed

- ORM:
    - Re-engineered Query API to use a unified expression-based model for consistency.
    - Improved relationship mapping logic for Many-to-Many and reverse mappings.
    - Leveraged Java 21+ features for internal implementation and public API idioms.

### Removed

- ORM:
    - `TypeSafeDtoTableMapping` class has been replaced with metamodels.
