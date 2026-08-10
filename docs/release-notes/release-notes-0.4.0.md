### Litebridge 0.4.0 Release Notes

## [0.4.0] - 2026-08-10

Litebridge 0.4.0 introduces a significant architectural evolution with the implementation of a new internal Abstract 
Syntax Tree (AST) engine and a specialized query compilation pipeline. This release focuses on performance and 
extensibility, featuring a new query plan cache and highly optimized DTO mapping utilizing `MethodHandle`-based access. 
Alongside these core engine improvements, the Maven plugin and Spring integration have received numerous enhancements 
and bug fixes to ensure a more stable and efficient development experience.

### Added
- ORM:
    - New internal Abstract Syntax Tree (AST) for query representation (`QueryNode`, `SelectNode`, etc.), providing a robust foundation for query compilation and execution.
    - `QueryCompiler` for translating the AST into executable database specifications.
    - `QueryPlanCache` to cache and reuse compiled query plans, significantly improving performance for repeated queries.
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
    - Add `initDefaultValues` config parameter to initialise default values for fields representing columns with a default value defined in the database.
    - Add `primitiveNotNulls` config parameter to control whether primitive fields are generated if the database column is not nullable, if applicable.
    - Add `generateConstructors` config parameter to control whether constructors are generated for entities.

### Changed
- ORM:
    - Refactored `select()`, `update()`, `delete()`, and `insert()` fluent APIs to use the new AST-based engine.
    - Re-engineered `PersistenceFacade` to integrate with the query plan cache and compilation pipeline.
    - Optimised relationship path resolution and implicit join generation during query compilation.
    - Significantly optimised DTO mapping performance in `SelectSpecDtoMapper` through a multi-layered approach:
        - Introduced a "compiled" `MappingPlan` to pre-calculate mapping logic, enabling index-based row access ($O(1)$ column lookups).
        - Transitioned to `MethodHandle`-based DTO construction and field population to bypass standard reflection overhead.
        - Implemented a two-phase resolution process (Populate & Batch Resolve) to reduce mapping complexity from $O(N^2)$ to $O(N)$ for large result sets.
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