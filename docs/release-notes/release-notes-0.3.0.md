### Litebridge 0.3.0 Release Notes

Litebridge 0.3.0 is a significant update that greatly expands the flexibility and power of the ORM. This release focuses
on an advanced expression-based query API, comprehensive metamodel support, and new tooling to streamline developer
workflows.

#### Major Highlights

- **Advanced Query Engine**: Full support for SQL functions, nested expressions, and complex query clauses like
  `GROUP BY` and `HAVING`.
- **General-Form Query API**: A new `select().from(...)` style for more flexible querying, including partial DTO
  population and custom projection.
- **Litebridge Maven Plugin**: A new tool for reverse-engineering schemas into entities and generating metamodels for
  type-safe queries.
- **Type-Safe Metamodels**: Initial support for generated metamodels, enabling compile-time safety for query
  construction.
- **Native SQL Improvements**: Named bind parameter support for raw SQL execution.

#### Querying Enhancements

The query engine now supports a powerful expression-based API:

- **Select Expressions**: Use the `Fn` utility class for SQL functions (`AVG`, `COUNT`, `UPPER`, `SUBSTRING`, etc.) and
  aliasing.
- **Expanded SQL Clauses**: Support for `HAVING`, `GROUP BY`, and subselects.
- **ORM-Side Type Conversion**: Explicitly convert database results to target Java types using `Fn.convert()`.
- **Relationship Support**: Improved Many-to-Many relationship handling, including reverse mappings.

*Example: Advanced query with expressions and grouping*

```java
List<Row> results = litebridge.select(row(
                f("eyeColour"),
                convert(avg("age"), Double.class)))
        .from(Person.class)
        .groupBy("eyeColour")
        .having(count()).gt(5)
        .list();
```

#### Tooling & Integration

- **Maven Plugin**: Automates entity creation from existing databases and generates metamodels.
- **Spring Boot**: Enhanced autoconfiguration with automatic entity registration via classpath scanning.
- **Metamodels**: Leverage generated static fields for type-safe queries, reducing reliance on string-based field
  identifiers.

#### SQL & Database Support

- **Native SQL**: Raw SQL execution now supports named bind parameters:
  ```java
  litebridge.nativeSql().query("SELECT * FROM PERSON WHERE ID = :id", Map.of("id", 123L));
  ```
- **New Operators**: Added `LIKE` and `NOT IN` operators for advanced filtering.
- **Data Types**: Enhanced support for BLOB/CLOB and improved date/time conversion.

#### Modernization & Safety

- **Java 21+**: Leverages modern Java idioms and features throughout the core engine.
- **Null-Safety**: Full JSpecify (`@NullMarked`, `@Nullable`) annotation support across the public API for better
  developer experience and runtime reliability.

#### Removals

- `TypeSafeDtoTableMapping` class has been replaced with metamodels.
- All APIs that accepted `TypeSafeDtoTableMapping` have been removed.