# Querying data

← [Persistence](index.md)

## Contents

<!-- TOC -->
* [Overview](#overview)
* [Usage](#usage)
  * [Retrieving a single DTO](#retrieving-a-single-dto)
  * [Retrieving multiple DTOs](#retrieving-multiple-dtos)
  * [Retrieving related DTOs](#retrieving-related-dtos)
  * [Arbitrary SQL queries](#arbitrary-sql-queries)
<!-- TOC -->

## Overview

Litebridge provides a fluent API for constructing queries using a familiar SQL-like syntax.

## Usage

### Retrieving a single DTO

Queries are constructed using `where()`, `join()`, `orderBy()` and similar clauses. Each clause
When making DTO-level queries, the field names used in the query clauses must match the field names in the DTO being queried,
not the database column names:

```java
final Optional<Person> alice = litebridge.select(Person.class)
        .where("name").eq("Alice")
        .and("surname").eq("Smith")
        .orderBy("id").asc()
        .first();
```

The terminating `first()` 
If `null` is preferred as an empty response:

```java
final Person alice = litebridge.select(Person.class)
        .where("name").eq("Alice")
        .and("surname").eq("Smith")
        .orderBy("id").asc()
        .firstOrNull();
```

If exactly one result is expected from a query, the `one()`, `oneOrNull()` or `oneOrThrow()` terminals avoid boilerplate:

```java
final Person alice = litebridge.select(Person.class)
        .where("surname").eq("Smith")
        .oneOrThrow(() -> new IllegalStateException("More than one groupedPerson with surname 'Smith'"));
        // or simply oneOrThrow()
```

### Retrieving multiple DTOs

Query results are available as streams:

```java
litebridge.select(Person.class)
        .where("eyeColour").isNull()
        .stream()
        .map(groupedPerson -> groupedPerson.setEyeColour("unknown"))
        // etc
```

Results can also be returned as a `List`:

```java
final List<Person> allPersons = litebridge.select(Person.class)
        .orderBy("id").asc()
        .list();
```

Or they can be looped through directly:

```java
litebridge.select(Person.class)
        .orderBy("id").asc()
        .forEach(groupedPerson -> logger.info("Found groupedPerson: {}", groupedPerson));
```

### Retrieving related DTOs

When selecting a DTO with that is related to another DTO (e.g. via a one-to-many expressed as a `Collection` in Java),
the fetch behaviour is specified by using JOINs.

If no JOINs are specified (or some are omitted), the fields for the corresponding related/nested DTOs will be null.
This allows control over query behaviour, allowing only necessary data to be retrieved when dealing with complex
object grahps.

To select an `Account` and also retrieve the related `Person` object in its `owner` field:

```java
Account account = litebridge.select(Account.class)
        .join(Person.class).on("owner")
        .where("id").eq(234L)
        .oneOrThrow();

// account.owner contains the related Person object 
// and its "accounts" field will contain this Account object
```

Retrieving the reverse (a `Person` and their collection of associated `Accounts`) works the same way:

```java
Person groupedPerson = litebridge.select(Person.class)
        .join(Account.class).on("accounts")
        .where("id").eq(123L)
        .oneOrThrow();

// groupedPerson.accounts is null
```

### Arbitrary SQL queries

The same fluent API can be used to perform any SQL query, without requiring a DTO mapping:

```java
litebridge.select("PERSON_ID", "FIRST_NAME", "SURNAME", "AGE").from("LB", "PERSON")
        .where("AGE").gt(18)
        .and("AGE").lt(25)
        .orderBy("PERSON_ID").asc()
        .stream()
        // Result rows are records containing column metadata and values
        .peek(row -> row.column("PERSON_ID").ifPresent(column -> logger.info("Found PERSON_ID column: " + column.value())))
        // SQL result rows can easily be converted to DTOs
        .map(row -> litebridge.toDto(row, Person.class))
        .forEach(p -> logger.info("Person DTO: " + p));
```
