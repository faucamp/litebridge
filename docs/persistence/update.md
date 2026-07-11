# Updating data

← [Persistence](index.md)

Updates and inserts can be performed in a variety of ways:

- Implicitly by persisting a DTO via `litebridge.save(Object)`
- Explicitly by inserting a new DTO via `litebridge.insert(Object)`
- Explicitly by updating an existing DTO via `litebridge.update(Object)`
- Explicitly via a DTO-level query: `litebridge.update(Object, Function)`
- Explicitly via a SQL-level query: `litebridge.update(String, Function)`
- **Natively** via raw SQL: `litebridge.nativeSql().execute(String, ...)`

For the query-based APIs, the `Function` allows access to chain a sequence of update clauses,
similar to the [`select()`](select.md) API.

## Persisting DTOs

For general usage, it is sufficient to rely on `litebridge.save(Object)` to manage a DTO's
persistence state.

To persist a DTO, call the `save()` method on the `Litebridge` instance:

```java
Person person = new Persion();
person.setName("Bob");
person.setSurname("Jones");

// Inserts a record into the mapped table
litebridge.save(person);
```

Multiple DTOs can be saved at once:

```java
// Inserts 3 record into the mapped table
litebridge.save(person1, person2, person3);
```

To persist a DTO, simply invoke the `save()` method on the `Litebridge` instance:

```java
Person person = new Person();
person.setName("Alice");

// Inserts a record into the mapped table
litebridge.save(person);

person.setName("Bob");
// Updates the record's "name" mapped column
litebridge.save(person);
```

This will result in SQL INSERT and/or UPDATE statements, depending on whether the DTO is initially new or already persisted.

Litebridge uses **cascading saves** by default. If a DTO has relationships to other DTOs, those DTOs will also be saved when the parent DTO is saved.

DTOs must first be registered with Litebridge with via a database table mapping before they can be persisted.

## Explicit DTO inserts

If an explicit INSERT statement is desired, it can be performed via `litebridge.insert(Object)`:

```java
litebridge.insert(person);
```

This will attempt a SQL `INSERT` statement regardless of the input DTO's persisted state.

## Explicit DTO updates

If an explicit UPDATE statement is desired, it can be performed via `litebridge.update(Object)`:

```java
litebridge.update(person);
```

This will execute a SQL `UPDATE` statement regardless of the persistence state of the DTO. 

## Query-based updates

Like select statements, explicit updates can be performed via a fluent API that adapts to the context
of the query being performed. Both DTO-level and SQL-level queries can be performed.

Each update statement is constructed by specifying the DTO class or table name, then a query function.
The query function is a lambda that allows the query to be constructed using a chainable API.

### DTO-level query-based updates

```java
litebridge.update(Person.class, p -> p
        .set("name").to("John")
        .set("surname").to("Doe")
        .set("age").to(18)
        .where("age").gt(18));
```

When making DTO-level updates via a query, the identifiers used in the query clauses must match 
the field names in the DTO being queried and not the database column names, 
unless a more formal mapping is specified at query-time via a query expression.

#### Metamodels

[Metamodels](metamodels.md) provide a type-safe way to perform updates:

```java
import static org.example.meta.PersonMeta.*;

litebridge.update(Person.class, p -> p
        .set(eyeColour).to("green")
        .where(name).eq("Alice"));
```

### SQL-level updates

Updates may be perfomed on a SQL-level using the fluent API, without requiring a DTO mapping:

```java
litebridge.update("LB.PERSON", p -> p
        .set("AGE").to(50)
        .where("FIRST_NAME").eq("Bob"));
```

> [!NOTE]
> To execute raw SQL `UPDATE`, `INSERT`, `DELETE` or DDL strings without using the fluent API, see [Native SQL Execution](native-sql.md).

### Atomic operations

Atomic operations allow updating a column's value based on its current value in the database.
This is especially useful for avoiding race conditions in concurrent environments.

The following atomic operations are supported:

| Operation | Method | Description |
| :--- | :--- | :--- |
| **Increment** | `.increment()` | Increments the value by 1. |
| **Addition** | `.add(value)` | Adds the specified value to the current value. |
| **Subtraction** | `.minus(value)` | Subtracts the specified value from the current value. |
| **Multiplication** | `.multiply(value)` | Multiplies the current value by the specified value. |
| **Division** | `.divide(value)` | Divides the current value by the specified value. |
| **Modulo** | `.mod(value)` | Applies the modulo operator to the current value with the specified value. |

#### Examples

**Incrementing a value**

```java
litebridge.update(Person.class, p -> p
        .set("age").increment()
        .where("id").eq(123));
```

**Adding to a value**

```java
litebridge.update("LB.ACCOUNT", a -> a
        .set("BALANCE").add(100.0)
        .where("ACCOUNT_ID").eq("ACC-456"));
```

**Subtracting from a value**

```java
litebridge.update(Inventory.class, i -> i
        .set("stockCount").minus(5)
        .where("sku").eq("SKU-789"));
```

**Multiplication and division**

```java
litebridge.update(Portfolio.class, p -> p
        .set("valuation").multiply(1.05) // Increase by 5%
        .set("riskScore").divide(2)      // Halve the risk score
        .where("active").eq(true));
```

**Applying modulo**

```java
litebridge.update(Worker.class, w -> w
        .set("shardId").mod(10)
        .where("status").eq("IDLE"));
```

Atomic operations work across DTO-level and SQL-level updates.
