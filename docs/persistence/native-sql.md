# Native SQL Execution

← [Persistence](index.md)

Litebridge provides a native SQL API for executing raw SQL queries and statements directly against the database. 

Unlike Litebridge's [fluent API](select.md), which parses and transforms queries to support features like DTO mapping and SQL dialect abstraction, the native SQL API passes statements through to the underlying database with minimal intervention.

This is useful for:
- Executing complex or vendor-specific SQL that is not supported by the fluent API.
- Performance-critical operations where ORM overhead must be bypassed.
- Performing DDL operations (e.g., `CREATE TABLE`, `DROP INDEX`).
- Using database-specific features like CTEs, window functions, or specialized procedures.

## Accessing the API

The native SQL API is accessed via the `litebridge.nativeSql()` method. It provides two main methods:
- `query()`: For executing SQL `SELECT` statements that return a result set.
- `execute()`: For executing SQL `INSERT`, `UPDATE`, `DELETE`, or DDL statements that return an update count.

## Querying data

The `query()` method returns a `List<Row>`, similar to the fluent SQL-level select API.

### Positional parameters

Positional parameters are specified using `?` in the SQL string, and values are provided as a list or variable-length array of arguments:

```java
import org.litebridge.db.spi.Row;

import java.util.List;

// Using varargs as bind parameters 
List<Row> rows = litebridge.nativeSql().query(
        "SELECT * FROM PERSON WHERE FIRST_NAME LIKE ? AND SURNAME = ?",
        "A%", "Smith");

        // Use a list of bind parameters
        List<Object> params = List.of("A%", "Smith");
        List<Rows> rows2 = litebridge.nativeSql().query(
                "SELECT * FROM PERSON WHERE FIRST_NAME LIKE ? AND SURNAME = ?",
                params);

        // Query without bind parameters
        List<Rows> rows3 = litebridge.nativeSql().query("SELECT COUNT(*) FROM PERSON");
```

### Named parameters

Named parameters are specified using `:parameterName` in the SQL string, and values are provided as a `Map<String, Object>`:

```java
import org.litebridge.db.spi.Row;

import java.util.Map;

List<Row> rows = litebridge.nativeSql().query(
        "SELECT * FROM PERSON WHERE FIRST_NAME LIKE :firstName AND SURNAME = :surname",
        Map.of("firstName", "A%",
                "surname", "Smith"));
```

## Executing statements

The `execute()` method returns an `UpdateResult`, which contains the number of rows affected by the operation.

### Positional parameters

```java
import org.litebridge.db.spi.update.UpdateResult;

UpdateResult result = litebridge.nativeSql().execute(
        "INSERT INTO PERSON (PERSON_ID, FIRST_NAME, SURNAME) VALUES (?, ?, ?)",
        123L, "Alice", "Smith");

System.out.

println("Rows inserted: "+result.rowsAffected());
```

### Named parameters

```java
import org.litebridge.db.spi.update.UpdateResult;

import java.util.Map;

UpdateResult result = litebridge.nativeSql().execute(
        "UPDATE PERSON SET FIRST_NAME = :newName WHERE PERSON_ID = :id",
        Map.of("newName", "Bob", "id", 123L));

System.out.

println("Rows updated: "+result.rowsAffected());
```

## Mapping results to DTOs

While `nativeSql().query()` returns raw `Row` objects, Litebridge can still be used to map these rows to DTOs manually using the `toDto()` method:

```java
List<Person> people = litebridge.nativeSql().query("SELECT * FROM PERSON WHERE AGE > ?", 18)
        .stream()
        .map(row -> litebridge.toDto(row, Person.class))
        .toList();
```

## Transactions

Native SQL operations participate in Litebridge [transactions](transactions.md). If a transaction is active, the native SQL statement will be executed within that transaction.

```java
litebridge.transaction().execute(() -> {
    litebridge.nativeSql().execute("UPDATE ACCOUNT SET BALANCE = BALANCE - 100 WHERE ID = 1");
    litebridge.nativeSql().execute("UPDATE ACCOUNT SET BALANCE = BALANCE + 100 WHERE ID = 2");
});
```

## Native SQL vs SQL-level Fluent API

It is important to distinguish between Native SQL and the "SQL-level" fluent API (e.g., `litebridge.select().from("TABLE")`).

| Feature             | Fluent API (SQL-level)                                         | Native SQL                                                                 |
|:--------------------|:---------------------------------------------------------------|:---------------------------------------------------------------------------|
| **API Style**       | Fluent, chainable Java methods                                 | Raw SQL strings                                                            |
| **Parsing**         | Parsed and transformed by Litebridge                           | Passed directly to the database, except for handling named bind parameters |
| **Dialect Support** | Abstracted (H2, Postgres, etc.)                                | Vendor-specific SQL                                                        |
| **Type Safety**     | Low-medium (uses strings or query expressions for identifiers) | None                                                                       |
| **DTO Integration** | Manual (via `toDto()`)                                         | Manual (via `toDto()`)                                                     |
| **Validation**      | Some compile-time/runtime validation                           | Runtime only                                                               |
