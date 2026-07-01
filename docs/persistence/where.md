# Where Clauses

← [Querying data](select.md)

Litebridge provides a fluent API for building `WHERE` clauses in queries. These clauses are typically initiated 
by calling `.where("fieldName")` or `.where("columnName")` on a select query, followed by an operator.

## Operators

The following operators are available on condition clauses. Most operators support both direct values and sub-queries.

| Operator      | Method                | Description                                   |
|:--------------|:----------------------|:----------------------------------------------|
| `=`           | `.eq(value)`          | Equals                                        |
| `<>`          | `.neq(value)`         | Not equals                                    |
| `<`           | `.lt(value)`          | Less than                                     |
| `<=`          | `.lte(value)`         | Less than or equals                           |
| `>`           | `.gt(value)`          | Greater than                                  |
| `>=`          | `.gte(value)`         | Greater than or equals                        |
| `IS NULL`     | `.isNull()`           | Checks if a value is null                     |
| `IS NOT NULL` | `.isNotNull()`        | Checks if a value is not null                 |
| `LIKE`        | `.like(pattern)`      | Pattern matching using wildcards (`%`, `_`)   |
| `IN`          | `.in(values...)`      | Inclusion in a set of values or a sub-query   |
| `NOT IN`      | `.notIn(values...)`   | Exclusion from a set of values or a sub-query |

### Basic Examples

```java
// Equality
litebridge.select(Person.class).where("name").eq("Alice").list();

// Comparison
litebridge.select(Person.class).where("age").gt(18).list();

// Null checks
litebridge.select(Person.class).where("eyeColour").isNull().list();
```

### The IN and NOT IN operators

The `.in()` operator is overloaded to support various ways of specifying the set of values:

#### Varargs and Arrays
```java
// Inclusion
litebridge.select(Person.class)
    .where("id").in(1L, 2L, 3L)
    .list();

// Exclusion
litebridge.select(Person.class)
    .where("id").notIn(1L, 2L, 3L)
    .list();
```

#### Collections
```java
List<Long> ids = List.of(1L, 2L, 3L);

// Inclusion
litebridge.select(Person.class)
    .where("id").in(ids)
    .list();

// Exclusion
litebridge.select(Person.class)
    .where("id").notIn(ids)
    .list();
```

#### Sub-queries
You can use a lambda to define a sub-select query:
```java
// Inclusion
litebridge.select(Person.class)
    .where("id").in(sub -> sub.select("id")
        .from(Person.class)
        .where("name").eq("Alice"))
    .list();

// Exclusion
litebridge.select(Person.class)
    .where("id").notIn(sub -> sub.select("id")
        .from(Person.class)
        .where("name").eq("Alice"))
    .list();
```

### The LIKE operator

The `.like()` operator allows for pattern matching using SQL wildcards (`%` for any sequence of characters, `_` for a single character).

#### DTO-level Example
When using DTOs, use the field name:
```java
litebridge.select(Person.class)
    .where("name").like("Al%")
    .list();
```

#### SQL-level Example
When using raw SQL, use the column name:
```java
litebridge.select()
    .from("LB.PERSON")
    .where("SURNAME").like("%ohnso%")
    .list();
```

### Expression-based Conditions

For more advanced scenarios, such as when joining multiple tables and needing to disambiguate fields, you can use
the `f()` or `field()` selectors from the `Fn` class to target a specific DTO type present in the query:

```java
import static org.litebridgedb.orm.expression.Fn.f;

litebridge.select(Person.class)
    .join(Account.class).on("accounts")
    .join(Address.class).on("addresses")
    .where(f(Person.class, "id")).eq(1L)
    .and(f(Address.class, "id")).eq(123L)
    .oneOrThrow();
```

## Logical Operators

Conditions can be combined using `.and()` and `.or()`:

```java
litebridge.select(Person.class)
    .where("name").eq("Alice")
    .and("age").gte(21)
    .list();
```

By default, Litebridge chains these conditions. Complex grouping of conditions (using parentheses) is also supported 
via the functional `and()` and `or()` variants:

```java
litebridge.select(Person.class)
    .where("name").eq("Alice")
    .and(c -> c.where("age").lt(18).or("age").gt(65))
    .list();
```
