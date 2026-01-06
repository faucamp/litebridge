# litebridge

![Coverage](.github/badges/jacoco.svg)
![Branches](.github/badges/branches.svg)

Litebridge is a fast, lightweight Object-Relational Mapper (ORM) for Java 21+. It simplifies persistence by treating SQL as a first-class citizen, balancing relational power with the flexibility of OOP - without the "annotation hell" or complex toolchains of JPA.

> **Philosophy**: SQL-first, minimal magic. Litebridge favours programmatic configuration and developer intent over code-to-table generation and heavy abstraction.

## Key Features

* **Lightweight**: A pure Java library with minimal external dependencies.
* **Modern**: Built for Java 21+, leveraging modern idioms and features extensively.
* **Efficient**: Focuses on performance and minimising database round-trips via built-in DTO change tracking.
* **Transparent Mapping**: Map DTOs to databases without modifying your domain classes. Use programmatic, `Map`-based configuration or _optional_ annotations.
* **Fluent API**: Compose queries using a natural, SQL-like fluent builder that stays out of your way.

## Quick Start (Preview)

### Registering a DTO-table mapping

```java
// Get a litebridge instance
final Litebridge litebridge = new Litebridge(new H2DatabaseProvider(connection));

// Specify the table mapping for the Person DTO class
litebridge.register(Person .class, t("LB", "PERSON",Map.of(
        "id", c("PERSON_ID", true,"LB.PERSON_SEQ"),
        "name", c("FIRST_NAME"),
        "surname", c("SURNAME")));
```

### Persisting a DTO:

Persisting a DTO is performed by tracking it using the `track()` method, and calling `save()` to persist the changes:

```java
// Track changes to the Person DTO
final Person person = litebridge.track(new Person());

// Update some DTO properties
person.setName("Alice");
person.setSurname("Smith");
        
// Save the changes to the database
litebridge.save(person);
```

The resulting SQL statement (insert/update, and which columns to update) is determined automatically by Litebridge.

### Querying

Litebridge provides a fluent API for constructing queries using a familiar SQL-like syntax:

#### Retrieving a single DTO

```java
final Optional<Person> alice = litebridge.select(Person.class)
        .where("name").eq("Alice")
        .and("surname").eq("Smith")
        .orderBy("id").asc()
        .first();
```

Or, if you prefer `null` as an empty response and avoid the `Optional` wrapper:

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
        .where("id").eq(123)
        .oneOrThrow(() -> new IllegalArgumentException("No person with ID 123"));
        // or simply oneOrThrow()
```

#### Retrieving multiple DTOs

Query results are available as Java 8 streams:

```java
litebridge.select(Person.class)
        .where("eyeColour").isNotNull()
        .stream()
        .forEach(p -> logger.info("Person with eye colour (isNotNull): " + p));
```

Results can also be returned as a `List`:

```java
final List<Person> allPersons = litebridge.select(Person.class)
        .orderBy("id").asc()
        .list();
```


#### Arbitrary SQL queries

The same fluent API can be used to perform any SQL query, without requiring a DTO mapping:

```java
litebridge.select("PERSON_ID", "FIRST_NAME", "SURNAME", "AGE").from("LB", "PERSON")
        .where("AGE").gt(18)
        .and("AGE").lt(25)
        .orderBy("PERSON_ID").asc()
        .stream()
        // Result rows are generic records containing column metadata and values
        .peek(row -> row.column("PERSON_ID").ifPresent(column -> logger.info("Found PERSON_ID column: " + column.value())))
        // SQL result rows can easily be converted to DTOs
        .map(row -> litebridge.toDto(row, Person.class))
        .forEach(p -> logger.info("Person DTO: " + p));
```

## Project Structure

Litebridge is modular, allowing you to include only the components you need.

### Core modules

#### `litebridge-orm`

![Coverage](.github/badges/litebridge-orm/jacoco.svg)
![Branches](.github/badges/litebridge-orm/branches.svg)

The core engine. This is the primary dependency required for all applications using the ORM.

#### `litebridge-db`
A collection of database provider modules. You only need to include the specific implementation for your database (or multiple if needed).
* **`litebridge-db-h2`**:

  ![Coverage](.github/badges/litebridge-db/litebridge-db-h2/jacoco.svg)
  ![Branches](.github/badges/litebridge-db/litebridge-db-h2/branches.svg)

  H2 database provider.
* **`litebridge-db-spi`**:

  ![Coverage](.github/badges/litebridge-db/litebridge-db-spi/jacoco.svg)
  ![Branches](.github/badges/litebridge-db/litebridge-db-spi/branches.svg)

  The Service Provider Interface (SPI) for implementing custom database providers. Not required for client use.

#### `litebridge-tracking`

![Coverage](.github/badges/litebridge-tracking/jacoco.svg)
![Branches](.github/badges/litebridge-tracking/branches.svg)

Exposes the `ChangeTracker` API. This provides lightweight change tracking for arbitrary DTOs. While the ORM uses this internally for SQL optimization, it can be used independently for other state-tracking needs.

### Supporting modules

#### `litebridge-converter`

![Coverage](.github/badges/litebridge-converter/jacoco.svg)
![Branches](.github/badges/litebridge-converter/branches.svg)

Simple type conversion support for translating between Java types and SQL-specific types.

#### `litebridge-commons`

![Coverage](.github/badges/litebridge-commons/jacoco.svg)
![Branches](.github/badges/litebridge-commons/branches.svg)

Internal utilities. Litebridge implements internal versions of common patterns to avoid bloating your project with large 3rd-party utility suites.

### Documentaion and examples

#### `example`
Examples demonstrating how to use Litebridge.
