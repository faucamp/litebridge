# Updating data

← [Persistence](index.md)

Updates and inserts can be performed in a variety of ways:

- Implicitly by persisting a DTO via `litebridge.save(Object)`
- Explicitly by inserting a new DTO via `litebridge.insert(Object)`
- Explicitly by updating an existing DTO via `litebridge.update(Object)`
- Explicitly via a DTO-level query: `litebridge.update(Object, Function)`
- Explicitly via a SQL-level query: `litebridge.update(String, Function)`

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

### DTO-level query updates

```java
litebridge.update(Person.class, p -> p
        .set("name").to("John")
        .set("surname").to("Doe")
        .set("age").to(18)
        .where("age").gt(18));
```

When making DTO-level queries, the identifiers used in the query clauses must match the field names in the DTO being queried
and not the database column names, unless a more formal mapping is specified at query-time.

#### Atomic operations

The DTO-level update API supports atomic operations like increments:

```java
litebridge.update(Person.class, p -> p
        .set("age").increment()
        .where("surname").eq("Doe"));
```

Other available atomic operations include `add(value)`, `minus(value)`, `multiply(value)`, `divide(value)` and `mod(value)`.

#### Metamodel-level query updates

[Metamodels](metamodels.md) provide a type-safe way to perform updates:

```java
import static org.example.meta.PersonMeta.*;

litebridge.update(Person.class, p -> p
        .set(eyeColour).to("green")
        .where(name).eq("Alice"));
```

### SQL-level query updates

Updates may be perfomed on a SQL-level, without requiring a DTO mapping:

```java
litebridge.update("LB.PERSON", p -> p
        .set("AGE").to(50)
        .where("FIRST_NAME").eq("Bob"));
```

The SQL-level update API also supports atomic operations:

```java
litebridge.update("LB.PERSON", p -> p
        .set("AGE").add(5)
        .where("PERSON_ID").eq(123L));
```
