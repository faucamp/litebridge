# Persistence

← [Home](../index.md)

Litebridge provides a lightweight persistence layer for working with DTOs and relational databases. It focuses on mapping DTO classes to database tables, querying data, applying inserts and updates, deleting records, managing transactional boundaries, and using type-safe metamodels for queries.

## Persistence Topics

This section covers the core persistence features provided by Litebridge:

1. **[Registering DTO-table mappings](dto-table-mapping.md)**: Define how DTO classes map to database tables, columns, keys, and relationships.
2. **[Querying data](select.md)**: Retrieve records from the database and map results back into DTOs.
3. **[Metamodels](metamodels.md)**: Build type-safe queries using generated or manual metamodel classes.
4. **[Updating data](update.md)**: Persist new DTOs or update existing records using Litebridge’s persistence operations.
5. **[Deleting data](delete.md)**: Remove records from the database using mapped DTOs.
6. **[Change tracking](change-tracking.md)**: Optimise updates by only persisting modified fields.
7. **[Transactions](transactions.md)**: Coordinate persistence operations safely using Litebridge transaction support.

## Core Concepts

The persistence module is built around a few key ideas:

- **DTO-table mappings** describe how Java DTOs correspond to database tables.
- **Metamodels** provide a type-safe way to reference DTO fields in queries.
- **Persistence operations** provide a simple way to select, insert, update, save, and delete DTOs.
- **Change tracking** helps Litebridge determine which DTO fields need to be persisted.
- **Transactions** ensure related database operations are committed or rolled back together.