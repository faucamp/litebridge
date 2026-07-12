# Where Clauses

← [Querying data](select.md)

Litebridge provides a fluent API for building `WHERE` clauses in queries. These clauses are typically initiated 
by calling `.where("fieldName")` or `.where(PersonMeta.fieldName)` on a select query, followed by an operator.

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
// Equality (String-based)
litebridge.select(Person.class).where("name").eq("Alice").list();

// Equality (Type-safe Metamodel)
litebridge.select(Person.class).where(PersonMeta.name).eq("Alice").list();

// Comparison
litebridge.select(Person.class).where(PersonMeta.age).gt(18).list();

// Null checks
litebridge.select(Person.class).where(PersonMeta.eyeColour).isNull().list();
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
A lambda can be used to define a sub-select query:
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

For more advanced scenarios, such as when joining multiple tables and needing to disambiguate fields,
the `f()` or `field()` selectors from the `Fn` class can be used to target a specific DTO type present in the query:

```java
import static org.litebridge.orm.expression.Fn.f;

litebridge.select(Person .class)
    .

join(Account .class).

on("accounts")
    .

join(Address .class).

on("addresses")
    .

where(f(Person.class, "id")).

eq(1L)
    .

and(f(Address.class, "id")).

eq(123L)
    .

oneOrThrow();
```

Alternatively (and recommended), [metamodels](metamodels.md) can be used:

```java
import static org.litebridge.orm.expression.Fn.f;
import static org.example.meta.PersonMeta.*;
import static org.example.meta.AddressMeta.*;

Person person = litebridge.select(Person.class)
        .join(Account.class).on(accounts)
        .join(Address.class).on(addresses)
        .where(PersonMeta.id).eq(1L)
        .and(AddressMeta.id).eq(123L)
        .oneOrThrow();
```

The API allows using SQL function in where clauses via query expressions (though keep in mind that this should be used
with caution, as is the case when writing standard SQL):

```java
Person person = litebridge.select(Person.class)
                .where(PersonMeta.name.upper()).eq("ALICE")
                .oneOrThrow();
```

## Logical Operators

Conditions can be combined using `.and()` and `.or()`. These operators are available both as standard methods for simple chaining, and as functional variants for creating complex, nested conditions.

### Chaining Conditions

By default, conditions are chained together. Chaining `.and()` and `.or()` methods will result in a flat list of conditions in the generated SQL:

```java
litebridge.select(Person.class)
    .where("name").eq("Alice")
    .and("age").gte(21)
    .or("status").eq("VIP")
    .list();
```

SQL Equivalent: `... WHERE NAME = ? AND AGE >= ? OR STATUS = ?`

### Nested Conditions

For more complex logic involving precedence, Litebridge supports nesting conditions using lambdas. When a lambda is used with `.and()` or `.or()`, Litebridge encloses the conditions defined within that lambda in parentheses.

```java
litebridge.select(Person.class)
    .where("name").eq("Alice")
    .and(c -> c.where("age").lt(18).or("age").gt(65))
    .list();
```

SQL Equivalent: `... WHERE NAME = ? AND (AGE < ? OR AGE > ?)`

These logical operators and nesting capabilities are also available for `HAVING` clauses in aggregate queries.

#### Complex Nesting Example

Deeply nested conditions can be constructed to build highly specific filters:

```java
final Person result = litebridge.select(Person.class)
        .where("name").eq("Alice")
        .and(q -> q.where("surname").eq("Jones")
                   .or("age").eq(20)
                   .or(q2 -> q2.where("eyeColour").eq("green")
                               .and("age").gt(35)))
        .oneOrThrow();
```

SQL Equivalent:
```sql
SELECT ... FROM LB.PERSON WHERE FIRST_NAME = ? AND (SURNAME = ? OR AGE = ? OR (EYE_COLOUR = ? AND AGE > ?))
```