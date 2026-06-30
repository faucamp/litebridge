# Where Clauses

← [Querying data](select.md)

Litebridge provides a fluent API for building `WHERE` clauses in queries. These clauses are typically initiated 
by calling `.where("fieldName")` or `.where("columnName")` on a select query, followed by an operator.

## Operators

The following operators are available on condition clauses. Most operators support both direct values and sub-queries.

| Operator | Method | Description |
|:---|:---|:---|
| `=` | `.eq(value)` | Equals |
| `<>` | `.neq(value)` | Not equals |
| `<` | `.lt(value)` | Less than |
| `<=` | `.lte(value)` | Less than or equals |
| `>` | `.gt(value)` | Greater than |
| `>=` | `.gte(value)` | Greater than or equals |
| `IS NULL` | `.isNull()` | Checks if a value is null |
| `IS NOT NULL` | `.isNotNull()` | Checks if a value is not null |
| `IN` | `.in(values...)` | Inclusion in a set of values or a sub-query |

### Basic Examples

```java
// Equality
litebridge.select(Person.class).where("name").eq("Alice").list();

// Comparison
litebridge.select(Person.class).where("age").gt(18).list();

// Null checks
litebridge.select(Person.class).where("eyeColour").isNull().list();
```

### The IN Operator

The `.in()` operator is overloaded to support various ways of specifying the set of values:

#### Varargs and Arrays
```java
litebridge.select(Person.class)
    .where("id").in(1L, 2L, 3L)
    .list();
```

#### Collections
```java
List<Long> ids = List.of(1L, 2L, 3L);
litebridge.select(Person.class)
    .where("id").in(ids)
    .list();
```

#### Sub-queries
You can use a lambda to define a sub-select query:
```java
litebridge.select(Person.class)
    .where("id").in(sub -> sub.select("id")
        .from(Person.class)
        .where("name").eq("Alice"))
    .list();
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
