# Changelog

## 0.3.2

### Added
- Maven plugin: Added "resolveRelationships" config parameter to control foreign key behaviour.

### Changed
- Maven plugin:
  - Fixed issue with complex circular dependencies in the Litebridge Maven plugin's `reverse-engineer` goal.
  - Fix many-to-many join table indentification for tables with two columns (but aren't join tables).
  - Refactor `ReverseEngineerMojo` to improve performance

### Deprecated
- None.

### Removed
- None.

## 0.3.1

### Added
- None.

### Changed
- Renamed main `org.litebridgedb` package back to original `org.litebridge`
- JavaDocs expanded

### Deprecated
- None.

### Removed
- None.

## 0.3.0

### Added
- Advanced expression-based Query API supporting SQL functions (AVG, COUNT, UPPER, SUBSTRING, etc.).
- New `select().from(...)` general-form query syntax for partial DTO population and custom projections.
- Support for `GROUP BY` and `HAVING` clauses in fluent queries.
- Litebridge Maven Plugin for reverse-engineering schemas and generating type-safe metamodels.
- Type-safe query metamodels for compile-time validation.
- Named bind parameters in Native SQL queries.
- New query operators: `LIKE`, `NOT IN`.
- Enhanced data type support (BLOB/CLOB, improved date/time conversion).
- Automatic Spring Boot entity registration via classpath scanning.
- Full JSpecify null-safety annotation support (@NullMarked, @Nullable).

### Changed
- Re-engineered Query API to use a unified expression-based model for consistency.
- Improved relationship mapping logic for Many-to-Many and reverse mappings.
- Leveraged Java 21+ features for internal implementation and public API idioms.

### Deprecated
- None.

### Removed
- `TypeSafeDtoTableMapping` class has been replaced with metamodels.
