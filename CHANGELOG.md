# Changelog

## [Unreleased]

### Added
- Database Provider SPI:
  - `ColumnMetaData` now contains the default value of a column.
- Maven plugin: 
  - Add `resolveRelationships` config parameter to control foreign key behaviour.
  - Add `initDefaultValues` config parameter to initialise default values for fields representing columns with a default value defined in the database.
  - Add `primitiveNotNulls` config parameter to control whether primitive fields are generated if the database column is not nullable, if applicable.
  - Add `generateConstructors` config parameter to control whether constructors are generated for entities.

### Changed
- ORM:
  - Optimise DTO mapping performance in `SelectSpecDtoMapper` by implementing a two-phase resolution process. This reduces complexity from $O(N^2)$ to $O(N)$ for large result sets and improves handling of bidirectional relationships and Records.
  - Improve efficiency of collection mappings using a high-performance DTO list cache.
- Maven plugin:
  - Refactor `ReverseEngineerMojo` to improve performance
  - Initialise default values for fields representing columns with a default value defined in the database.
  - Use `joinUsing` instead of `joinOn` annotation parameter if applicable.
- Type converter:
  - Boolean converter now supports strings "1" and "0" as valid boolean values.

### Deprecated
- Database Provider SPI:
  - Deprecated SPI method: `ColumnMetadata.setAutoIncrement()`

### Fixed
- ORM:
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
