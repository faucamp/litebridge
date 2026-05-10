# Updating data

← [Persistence](index.md)

Updates to data can be performed in a variety of ways:

- Implicitly by persisting a DTO via `litebridge.save(Object)`
- Explicitly by updating a DTO via `litebridge.update(Object)`
- Explicitly via a DTO-level query: `litebridge.update(Object, Function)`
- Explicitly via a SQL-level query: `litebridge.update(String, Function)`

For the query-based APIs, the `Function` allows access to chain a sequence of update clauses,
similar to the [`select()`](select.md) API.

## Usage

#### Persistence

##### Persisting a DTO

For basic usage, it is sufficient to rely on `litebridge.save(Object)` to manage a DTO's
persistence state.

To persist a DTO, simply invoke the `save()` method on the `Litebridge` instance:

```java
Person person = getPersonFromSomewhere();
litebridge.save(person);

person.setName("Bob");
litebridge.save(person);
```

This will result in SQL INSERT and/or UPDATE statements, depending on whether the DTO is initially new or already persisted.

DTOs must first be registered with Litebridge with via a database table mapping before they can be persisted.

##### Explicit DTO updates

If an explicit UPDATE statement is desired, it can be performed via `litebridge.update(Object)`:

```java
litebridge.update(person);
```

This will attempt a SQL UPDATE statement regardless of the persistence state of the DTO.

#### Query-based updates

Like select statements, explicit updates can be performed via a fluent API that adapts to the context
of the query being performed. Both DTO-level and SQL-level queries can be performed.

Each update statement is constructed by specifying the DTO class or table name, then a query function.
The query function is a lambda that allows the query to be constructed using a chainable API.

###### DTO-level query updates

```java
litebridge.update(Person.class, p -> p
        .set("name").to("John")
        .set("surname").to("Doe")
        .where("age").gt(18));
```

When making DTO-level queries, the identifiers used in the query clauses must match the field names in the DTO being queried
and not the database column names, unless a more formal mapping is specified at query-time.

###### SQL-level query updates

```java
litebridge.update("LB.PERSON", p -> p
        .set("AGE").to(50)
        .where("FIRST_NAME").eq("Bob"));
```
