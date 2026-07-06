# Query Expressions

← [Querying data](select.md)

Litebridge uses query expressions to define exactly what should be retrieved from the database.
Expressions range from simple column selections to invoking SQL functions and ORM-side type conversions.

Expressions are primarily constructed using static methods from the `org.litebridgedb.orm.expression.Fn` utility class.

## Selectors

Selectors are the basic building blocks for choosing which fields or columns to retrieve.

| Method                                        | Description                                                         |
|:----------------------------------------------|:--------------------------------------------------------------------|
| `f(String)`, `field(String)`                  | Selects a field from the primary DTO being queried. Used in DTO-level queries. |
| `f(Class, String)`, `field(Class, String)`    | Selects a field from a specific DTO type. Useful in multi-table joins.         |
| `Metamodel.field`                             | Selects a field via its [Metamodel](metamodels.md). This is the recommended type-safe way to select fields. |
| `c(String)`, `column(String)`                 | Selects a database column by name.                                             |
| `ca(...)`, `columnAlias(...)`                 | Selects a database column and assigns it an alias.                  |

### DTO Field Examples

```java
import static org.litebridgedb.orm.expression.Fn.*;
import static org.example.meta.PersonMeta.id;
import static org.example.meta.PersonMeta.firstName;

// Select specific fields from a DTO using the shorthand field selector
List<Person> persons1 = litebridge.select(f("id"), f("name")).from(Person.class).list();

// Equivalent expression using metamodel fields (recommended)
List<Person> persons2 = litebridge.select(id, firstName).from(Person.class).list();

```

### Column Aliasing Examples

```java
import static org.litebridgedb.orm.expression.Fn.*;

// Simple alias
litebridge.select(columnAlias("FIRST_NAME", "firstName")).

from("LB.PERSON").

list();

// Alias with table name
litebridge.

select(ca("LB.PERSON", "PERSON_ID","id")).

list();
```

## SQL Functions

Litebridge supports common SQL functions that can be used in select statements.
Most functions can be nested.

### Aggregate Functions

Aggregate functions operate on a set of values and return a single value.

| Method        | SQL Equivalent | Description                 |
|:--------------|:---------------|:----------------------------|
| `avg(column)` | `AVG(column)`  | Returns the average value.  |
| `max(column)` | `MAX(column)`  | Returns the maximum value.  |
| `min(column)` | `MIN(column)`  | Returns the minimum value.  |
| `count()`     | `COUNT(*)`     | Returns the number of rows. |

### Scalar Functions

Scalar functions operate on a single value and return a single value based on the input.

| Method                            | SQL Equivalent   | Description                             |
|:----------------------------------|:-----------------|:----------------------------------------|
| `upper(column)`                   | `UPPER(column)`  | Converts a string to uppercase.         |
| `lower(column)`                   | `LOWER(column)`  | Converts a string to lowercase.         |
| `substring(column, start, [len])` | `SUBSTRING(...)` | Extracts a substring.                   |
| `abs(column)`                     | `ABS(column)`    | Returns the absolute value of a number. |

### System Functions

| Method               | SQL Equivalent      | Description                                 |
|:---------------------|:--------------------|:--------------------------------------------|
| `currentTimestamp()` | `CURRENT_TIMESTAMP` | Returns the current database date and time. |

### Nesting Functions

Functions can be nested to perform complex operations:

```java
import static org.litebridgedb.orm.expression.Fn.*;
import org.example.meta.PersonMeta;

// Get the uppercase of the first 3 characters of the surname using metamodel
litebridge.select(upper(substring(PersonMeta.surname, 1, 3)))
        .from(Person.class)
        .list();
```

## Type conversion expressions

These expressions instruct the ORM to convert the result of an expression to a specific type.
These are not a SQL-level conversion (like `CAST`), but rather an instruction to Litebridge to use its registered type
converters to transform the result before returning it to your application.

This is useful when the database driver returns a type that doesn't perfectly match what the client application expects
(e.g., a `BigDecimal` from `AVG()` when the client application wants a `Double`).

There are two main variants: `convert()` on a single nested expression, and `convert()` on a collection of expressions;
the latter of which is also exposed as `row()` for shorthand when a `Row` type is desired.

| Method                                 | Description                                                                                                               |
|:---------------------------------------|:--------------------------------------------------------------------------------------------------------------------------|
| `convert(ExpressionSpec, Class<T>)`    | Converts the result of the nested expression to the target class                                                          |
| `convert(Class<T>, ExpressionSpec...)` | Converts/projects the final result of all expressions to a target type                                                    |
| `row(...)`                             | Returns a generic `Row` result set for multi-expression selections. Shorthand for `convert(Row.class, ExpressionSpec...)` |

### Row results

`convert()` can be used to return generic `Row` objects. This is typically used with `row()` when
selecting multiple expressions:

```java
import org.litebridgedb.db.spi.Row;

import static org.litebridgedb.orm.expression.Fn.*;

// Returns a list of Row objects with two columns with types Integer and Long
List<Row> results = litebridge.select(row(
                convert(f("age"), Integer.class),
                convert(count(), Long.class)))
        .from(Person.class)
        .groupBy("age")
        .list();
```
