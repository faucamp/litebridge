# Querying data

← [Persistence](index.md)

Litebridge provides a fluent API for constructing queries using a familiar SQL-like syntax.

Select statements are constructed via the `litebridge.select()` method. The API adapts to what is being queried,
covering use cases for both DTO-marshalling and raw SQL queries.

Each query is built-up using a chained sequence of methods corresponding to the SQL clauses. The query is executed
by invoking a terminal method, which defines how results must be returned.

Litebridge supports five main types of select queries:
- **DTO-level queries**: Selecting full DTO objects from registered tables.
- **SQL-level queries**: Selecting specific columns or `*` from any table using a fluent API.
- **Expression-level queries**: Selecting specific SQL expressions, functions, or using ORM-side conversions.
- **Metamodel-level queries**: Using type-safe metamodel fields to build queries.
- **Native SQL queries**: Executing raw SQL strings directly via `litebridge.nativeSql().query()`.

## Usage

The following example demonstrates DTO-level and SQL-level queries:

```java
import org.litebridge.orm.expression.Fn;

// Simple string-based API
Optional<Person> alice1 = litebridge.select(Person.class)
        .where("name").eq("Alice")
        .and("surname").eq("Smith")
        .orderBy("id").asc()
        .first();

        // Equivalent type-safe metamodel query
        Optional<Person> bob = litebridge.select(Person.class)
                .where(PersonMeta.name).eq("Alice")
                .and(PersonMeta.surname).eq("Smith")
                .orderBy(PersonMeta.id).asc()
                .first();

        // Equivalent query using query expressions
        Optional<Person> mark = litebridge.select(Person.class)
                .where(Fn.f("name")).eq("Alice")
                .and(Fn.f("surname")).eq("Smith")
                .orderBy(Fn.f("id")).asc()
                .first();

        // Equivalent SQL-level query
        Optional<Row> henry = litebridge.select().from("LB.PERSON")
                .where("FIRST_NAME").eq("Alice")
                .and("SURNAME").eq("Smith")
                .orderBy("ID").asc()
                .first();
```

This will result in a SQL query similar to:

```sql
SELECT * FROM LB.PERSON WHERE FIRST_NAME = 'Alice' AND SURNAME = 'Smith' ORDER BY PERSON_ID ASC LIMIT 1;
```

Note that the example above made a DTO-level query by selecting the `Person` DTO class.
When making DTO-level queries, the identifiers used in the query clauses must match the field names in the DTO being queried
and not the database column names, unless a more formal mapping is specified at query-time.

The `first()` call is a terminal operation that executes the query and returns the result.

## Terminal operations

Terminal operations are the last step in a query and define how results must be returned.

### Get first result

The `first*` suite of terminals provides easy ways to get the first result from a query resulting in any number of records:

- `first()` returns an `Optional` with the first result if any rows were found, or an empty `Optional` otherwise
- `firstOrNull()` returns `null` if no rows were found, or the first result otherwise
- `firstOrThrow()` throws a `NoSuchElementException` if no rows were found
- `firstOrThrow(Supplier<Throwable>)` allows a custom exception to be thrown if no rows were found

### Get exactly one result

The `one*` suite of terminals provides easy ways to get the result of a query resulting in exactly one record.
They throw an `IllegalStateException` if multiple rows are returned from the database.

- `one()` returns an `Optional` with the single result if one matched, or an empty `Optional` otherwise
- `oneOrNull()` returns `null` if no rows were found, or the single result otherwise
- `oneOrThrow()` throws a `NoSuchElementException` if no rows were found
- `oneOrThrow(Supplier<Throwable>)` allows a custom exception to be thrown if no rows were found

### All results

The remaining terminals provide various ways to iterate/retrieve all results from a query:

- `stream()` returns a `Stream` of the results
- `list()` returns a `List` containing all results
- `forEach(Consumer<T>)` iterates over the results and applies the given consumer to each row


## DTO-level queries

### Setup

DTOs must first be registered with Litebridge with via a database table mapping before they can be queried.
Once a DTO class is registered, it can be queried using the Litebridge DTO select API.

### Basic querying

The `litebridge.select(Class<?>)` method is the basic entry point for DTO-level queries, and returns fully-populated
DTOs (dependent on join conditions/strategy if applicable):

```java
// List contains fully-populated Person objects
List<Person> persons = litebridge.select(Person.class).list();

// Retrieve a single fully-populated Person object
Person person = litebridge.select(Person.class).where("id").eq(123L).oneOrThrow();
```

To fetch multiple related DTOs in a single query, `join()` calls can be chained:

```java
Person person = litebridge.select(Person.class)
        .join(Account.class).on("accounts")
        .join(Address.class).on("addresses")
        .where("id").eq(123L)
        .oneOrThrow();
```

The `litebridge.select(Class<?>)` method is a shorthand equivalent to 
the general-form Litebridge "select from" call with no arguments:

```java
litebridge.select().from(Person.class).list();
```

### General-form querying

Partially-populated DTOs can be retrieved using the general-form Litebridge "select from" method:

```java
// Retrieve a single Person object with only the "id" and "age" fields populated
Person personIdAgeOnly = litebridge.select("id", "age").from(Person.class).oneOrThrow();
```

[Query expressions](#advanced-query-expressions) and [Metamodels](metamodels.md) can be used with general-form queries:

```java
import static org.litebridge.orm.expression.Fn.*;
import static org.example.meta.PersonMeta.*;

// Count the number of Person entites matching the query
Long personCount = litebridge.select(count()).from(Person.class).oneOrThrow();

        // Select the highest age from Person.age
        Long maxAge = litebridge.select(max("age")).from(Person.class).oneOrThrow();

        // Select the highest age from Person.age combining a query expression and metamodel
        Long maxAge = litebridge.select(max(age)).from(Person.class).oneOrThrow();

        // Select the highest age from Person.age using only the metamodel
        Long maxAge = litebridge.select(age.max()).from(Person.class).oneOrThrow();
```

## SQL-level queries

SQL-level queries allow performing SQL queries using Litebridge's fluent API, without requiring table registration or a DTO mapping. 

Specific columns can be selected using the `litebridge.select(String...)` method, or all columns can be selected using `litebridge.select()`:

> [!NOTE]
> To execute raw SQL strings without using the fluent API, see [Native SQL Execution](native-sql.md).

```java
// Selects all columns from the PERSON table
litebridge.select().from("LB.PERSON").list();

// Selects specific columns from the PERSON table
litebridge.select("PERSON_ID", "FIRST_NAME").from("LB.PERSON").list();

// Select with a JOIN USING clause
litebridge.select("FIRST_NAME", "ACCOUNT_NAME")
        .from("LB.PERSON")
        .join("LB.ACCOUNT").using("PERSON_ID")
        .list();
```

## Query Expressions and metamodels

Advanced queries can be constructed using query expressions.

These allow selecting SQL functions, aliased columns, or performing ORM-side type conversions. 
They work in both DTO-level and SQL-level contexts.

Expressions are primarily created using the `Fn` utility class. 
See the [Query Expressions](expressions.md) page for a full list of available expressions.

```java
import static org.litebridge.orm.expression.Fn.*;

// Select count of rows
Long count = litebridge.select(count()).from(Person.class).oneOrThrow();

        // Select with SQL functions
        List<Row> names = litebridge.select(upper("FIRST_NAME")).from("LB.PERSON").list();

        // Select with ORM-side type conversion
        Double avgAge = litebridge.select(convert(avg("age"), Double.class)).from(Person.class).oneOrThrow();
```

### Metamodels

Metamodels extend expressions and provide type safety for DTO-level queries: 

```java
// Import PersonMeta static fields, including "id", "name", "surname" and "age"
import static org.example.meta.PersonMeta.*;

// Retrieve and populate the "id", "name" and "surname" fields
// The value of "surname" is converted to uppercase with an UPPER() SQL function
Person person = litebridge.select(id, name, surname.upper())
        .from(Person.class)
        .where(age).gt(18)
        .list();
```

Refer to the [metamodel documentation](metamodels.md) for more information.

## DTO-level examples

### Retrieving a single DTO

To retrieve the first result:

```java
final Optional<Person> alice = litebridge.select(Person.class)
        .where("name").eq("Alice")
        .and("surname").eq("Smith")
        .orderBy("id").asc()
        .first();
```

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

Or they can be iterated through directly:

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
object graphs.

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
Person person = litebridge.select(Person.class)
        .join(Account.class).on("accounts")
        .where("id").eq(123L)
        .oneOrThrow();

// person.accounts contains the related Account objects
```

#### Query-specific Related DTO Strategy

The strategy for handling related DTOs can also be overridden on a per-query basis by passing a `RelatedDtoStrategy` to the `select()` method:

```java
import org.litebridge.orm.config.RelatedDtoStrategy;

// Even if no JOIN is specified, the "owner" field will be partially populated with a Person instance containing its ID
Account account = litebridge.select(Account.class, RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN)
        .where("id").eq(234L)
        .oneOrThrow();
```

This override takes precedence over the global configuration. See [Configuration](configuration.md) for more details on available strategies.

#### Joining multiple DTOs

Multiple related DTOs can be fetched in a single query by chaining `join()` calls:

```java
// String-based example
Person person = litebridge.select(Person.class)
        .join(Account.class).on("accounts")
        .join(Address.class).on("addresses")
        .where("id").eq(123L)
        .oneOrThrow();

// Metamodel-based example
Person person = litebridge.select(Person.class)
        .join(Account.class).on(PersonMeta.accounts)
        .join(Address.class).on(PersonMeta.addresses)
        .where(PersonMeta.id).eq(123L)
        .oneOrThrow();

// person.accounts and person.addresses are both populated
```

Queries can also filter results based on fields of joined DTOs by the `f(Class, String)` expression:

```java
import static org.litebridge.orm.expression.Fn.f;

Person person = litebridge.select(Person.class)
        .join(Account.class).on("accounts")
        .join(Address.class).on("addresses")
        .where(f(Person.class, "id")).eq(1L)
        .and(f(Address.class, "id")).eq(123L)
        .oneOrThrow();
```

Or by using [metamodels](metamodels.md):

```java
import static org.example.meta.PersonMeta.*;
import static org.example.meta.AddressMeta.*;

Person person = litebridge.select(Person.class)
        .join(Account.class).on(accounts)
        .join(Address.class).on(addresses)
        .where(PersonMeta.id).eq(1L)
        .and(AddressMeta.id).eq(123L)
        .oneOrThrow();
```

#### Many-to-Many JOINs

Many-to-many relationships can also be fetched using the same `join()` API:

```java
Group group = litebridge.select(Group.class)
        .join(Person.class).on("members")
        .where("name").eq("Administrators")
        .oneOrThrow();

// group.members contains the Person objects in this group
```

## Where Clauses

Query filters are defined using `where()`, `and()` and `or()` clauses.

A complete list of available operators (such as `.eq()`, `.gt()`, `.in()`, etc.) and examples of how to build complex 
conditions can be found on the [Where Clauses](where.md) page.

## SQL-level examples

The same fluent API can be used to perform any SQL query, without requiring a DTO mapping:

```java
litebridge.select("PERSON_ID", "FIRST_NAME", "SURNAME", "AGE").from("LB.PERSON")
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

Joins work the same way they do for DTO-level queries:

```java
litebridge.select("FIRST_NAME", "ACCOUNT_NAME")
        .from("LB.PERSON")
        .join("LB.ACCOUNT").using("PERSON_ID")
        .list();
```

The `litebridge.toDto(Row, Class)` method is a convenient way to manually map a raw SQL result row to a registered DTO class.

Note the difference between the identifiers used in the query clauses vs the previous DTO-level examples.
When making a SQL-level query, the identifiers used in the query clauses must match the column names in the database
table being queried; in the DTO examples, the identifiers are mapped DTO field names.

## Advanced Query Expressions

Query expressions (via the `Fn` class) provide a powerful way to interact with the database beyond simple column selection.

A complete list of available expressions and functions can be found on the [Query Expressions](expressions.md) page.

### SQL Functions

Common SQL functions are supported and can be nested.

```java
import static org.litebridge.orm.expression.Fn.*;

// Aggregates
litebridge.select(avg("age")).

from(Person .class).

oneOrThrow();
litebridge.

select(min("age")).

from(Person .class).

oneOrThrow();
litebridge.

select(max("age")).

from(Person .class).

oneOrThrow();
litebridge.

select(count()).

from(Person .class).

oneOrThrow();

// String functions
litebridge.

select(upper("name")).

from(Person .class).

list();
litebridge.

select(lower("name")).

from(Person .class).

list();
litebridge.

select(substring("name", 1,3)).

from(Person .class).

list();

// Math functions
litebridge.

select(abs("balance")).

from(Account .class).

list();
```

### ORM-side Type Conversion

Litebridge allows the Java type returned by an expression to be overridden using `Fn.convert()`. 
This uses Litebridge's registered type converters to transform the database result before it is returned to the client application.

`Fn.convert()` supports two main use cases:

1. **Result Type Override**: Overrides the return type of the fluent query API when a single expression is selected.
2. **Nested Expression Conversion**: Converts specific expressions when multiple values are selected (e.g., into a `Row`).

See the [Query Expressions](expressions.md#type-conversion-expressions) page for more details.

#### Result Type Override

When selecting a single expression, the exact type that the terminal methods should return can be specified:

```java
import static org.litebridge.orm.expression.Fn.*;

// Convert AVG() result to a Double instead of the database default (e.g. BigDecimal)
Double avgAge = litebridge.select(convert(avg("age"), Double.class))
        .from(Person.class)
        .oneOrThrow();

        // Convert the result of COUNT() to a String
        String countStr = litebridge.select(convert(count(), String.class))
                .from(Person.class)
                .oneOrThrow();
```

#### Generic Row results

`Fn.convert()` can also be used to return generic `Row` objects for more complex queries that do not map back to a single DTO or a simple type.
This is typically done via the shorthand method `Fn.row()` to select multiple expressions.

When selecting multiple expressions into a `Row`, `convert()` can ensure each column has the desired type:

```java
import org.litebridge.db.spi.Row;

import static org.litebridge.orm.expression.Fn.*;

List<Row> results = litebridge.select(row(
                convert(f("age"), Integer.class),
                convert(count(), Long.class)))
        .from(Person.class)
        .groupBy("age")
        .list();
```

### Grouping and Having

The select API supports `groupBy()` and `having()` clauses for aggregate queries. Just like `WHERE` clauses, `HAVING` clauses support `.and()`, `.or()`, and nested conditions:

```java
import static org.litebridge.orm.expression.Fn.*;

List<Row> results = litebridge.select(row(f("eyeColour"), count()))
        .from(Person.class)
        .groupBy("eyeColour")
        .having(count()).gt(5)
        .or(c -> c.where(f("eyeColour")).eq("blue").and(count()).gt(0))
        .list();
```

### Column Aliasing

Column selections can be aliased, which is especially useful when selecting from multiple tables 
or when using expressions in "raw SQL" queries:

```java
import static org.litebridge.orm.expression.Fn.*;

litebridge.select(
        columnAlias("FIRST_NAME", "firstName"),

columnAlias("SURNAME","lastName")
).

from("LB.PERSON").

list();
```

When using `columnAlias()`, the resulting `Row` will contain the specified aliases as column names.