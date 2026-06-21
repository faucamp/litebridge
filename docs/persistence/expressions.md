# Query Expressions

← [Querying data](select.md)

Litebridge uses query expressions to define exactly what should be retrieved from the database. 
Expressions range from simple column selections to invoking SQL functions and ORM-side type conversions.

Expressions are primarily constructed using static methods from the `org.litebridgedb.orm.expression.Fn` utility class.

## Selectors

Selectors are the basic building blocks for choosing which fields or columns to retrieve.

| Method | Description |
| :--- | :--- |
| `f(String)`, `field(String)` | Selects a DTO field by name. Used in DTO-level queries. |
| `c(String)`, `column(String)` | Selects a database column by name. |
| `c(String, String)`, `column(String, String)` | Selects a database column with a table name (e.g., `Table.Column`). |
| `ca(...)`, `columnAlias(...)` | Selects a database column and assigns it an alias. |

### DTO Field Examples
```java
import static org.litebridgedb.orm.expression.Fn.*;

// Select specific fields from a DTO using the shorthand field selector
List<Person> persons = litebridge.select(f("id"), f("name")).from(Person.class).list();
```

### Column Aliasing Examples

```java
import static org.litebridgedb.orm.expression.Fn.*;

// Simple alias
litebridge.select(columnAlias("FIRST_NAME", "firstName")).from("LB.PERSON").list();

// Alias with table name
litebridge.select(ca("LB.PERSON", "PERSON_ID", "id")).list();
```

## SQL Functions

Litebridge supports common SQL functions that can be used in select statements. 
Most functions can be nested.

### Aggregate Functions

Aggregate functions operate on a set of values and return a single value.

| Method | SQL Equivalent | Description |
| :--- | :--- | :--- |
| `avg(column)` | `AVG(column)` | Returns the average value. |
| `max(column)` | `MAX(column)` | Returns the maximum value. |
| `min(column)` | `MIN(column)` | Returns the minimum value. |
| `count()` | `COUNT(*)` | Returns the number of rows. |

### Scalar Functions

Scalar functions operate on a single value and return a single value based on the input.

| Method | SQL Equivalent | Description |
| :--- | :--- | :--- |
| `upper(column)` | `UPPER(column)` | Converts a string to uppercase. |
| `lower(column)` | `LOWER(column)` | Converts a string to lowercase. |
| `substring(column, start, [len])` | `SUBSTRING(...)` | Extracts a substring. |
| `abs(column)` | `ABS(column)` | Returns the absolute value of a number. |

### System Functions

| Method | SQL Equivalent | Description |
| :--- | :--- | :--- |
| `currentTimestamp()` | `CURRENT_TIMESTAMP` | Returns the current database date and time. |

### Nesting Functions

Functions can be nested to perform complex operations:

```java
import static org.litebridgedb.orm.expression.Fn.*;

// Get the uppercase of the first 3 characters of the surname
litebridge.select(upper(substring("surname", 1, 3))).from(Person.class).list();
```

## ORM-side Conversion

Litebridge provides explicit type conversion on the ORM side using `Fn.convert()`. This is not a SQL-level conversion (like `CAST`), but rather an instruction to Litebridge to use its registered type converters to transform the result before returning it to your application.

This is useful when the database driver returns a type that doesn't perfectly match what the client application expects
(e.g., a `BigDecimal` from `AVG()` when the client application wants a `Double`).

```java
import static org.litebridgedb.orm.expression.Fn.*;

// Calculate average age and ensure it's returned as a Double
Double avgAge = litebridge.select(convert(avg("age"), Double.class))
        .from(Person.class)
        .oneOrThrow();

// Get a count as a String
String countStr = litebridge.select(convert(count(), String.class))
        .from(Person.class)
        .oneOrThrow();
```
