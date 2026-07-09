# Persistence

← [Home](../index.md)

Litebridge provides a lightweight persistence layer for working with DTOs and relational databases. It focuses on
mapping DTO classes to database tables, querying data, applying inserts and updates, deleting records, managing
transactional boundaries, and using type-safe metamodels for queries.

## Persistence Topics

This section covers the core persistence features provided by Litebridge:

1. **[Registering DTO-table mappings](dto-table-mapping.md)**: Define how DTO classes map to database tables, columns,
   keys, and relationships.
    1. **[Entity annotations](entity-annotations.md)**: Alternative annotation-based mapping of tables to entity
       classes.
2. **[Querying data](select.md)**: Retrieve records from the database and map results back into DTOs.
    1. **[Where clauses](where.md)**: Details on constructing `WHERE`/`HAVING` clauses.
    2. **[Query expressions](query-expressions.md)**: Details on constructing advanced `SELECT`/`JOIN`/`GROUP BY`
       clauses.
    3. **[Metamodels](metamodels.md)**: Build type-safe queries using generated or manual metamodel classes.
    4. **[Native SQL](native-sql.md)**: Execute raw SQL queries and statements directly bypassing the ORM.
3. **[Updating data](update.md)**: Persist new DTOs or update existing records using Litebridge’s persistence
   operations.
4. **[Deleting data](delete.md)**: Remove records from the database using mapped DTOs.
5. **[Change tracking](change-tracking.md)**: Optimise updates by only persisting modified fields.
6. **[Transactions](transactions.md)**: Coordinate persistence operations safely using Litebridge transaction support.

## Core Concepts

The persistence module is built around a few key ideas:

- **DTO-table mappings** describe how Java DTOs correspond to database tables.
- **Metamodels** provide a type-safe way to reference DTO fields in queries.
- **Persistence operations** provide a simple way to select, insert, update, save, and delete DTOs.
- **Change tracking** helps Litebridge determine which DTO fields need to be persisted.
- **Transactions** ensure related database operations are committed or rolled back together.