# Querying data

← [Persistence](index.md)

Litebridge provides a fluent API for constructing queries using a familiar SQL-like syntax.

## Usage

Select statements are constructed via the `litebridge.select()` method. The API adapts to what is being queried,
covering use cases for both DTO-marshalling and raw SQL queries.

Each query is built-up using a chained sequence of methods corresponding to the SQL clauses. The query is executed
by invoking a terminal method, which defines how results must be returned.

Litebridge supports three main types of select queries:
- **DTO-level queries**: Selecting full DTO objects from registered tables.
- **SQL-level queries**: Selecting specific columns or `*` from any table.
- **Expression-level queries**: Selecting specific SQL expressions, functions, or using ORM-side conversions.

### Usage

```java
final Optional<Person> alice = litebridge.select(Person.class)
        .where("name").eq("Alice")
        .and("surname").eq("Smith")
        .orderBy("id").asc()
        .first();
```

This will result in a SQL query similar to:

```sql
SELECT * FROM LB.PERSON WHERE FIRST_NAME = 'Alice' AND SURNAME = 'Smith' ORDER BY ID ASC LIMIT 1;
```

Note that the example above made a DTO-level query by selecting the `Person` DTO class.
When making DTO-level queries, the identifiers used in the query clauses must match the field names in the DTO being queried
and not the database column names, unless a more formal mapping is specified at query-time.

The `first()` call is a terminal operation that executes the query and returns the result.

#### DTO-level queries

DTOs must first be registered with Litebridge with via a database table mapping before they can be queried.

Once a DTO class is registered, it can be queried using the `litebridge.select(Class<?>)` method, e.g.:

```java
litebridge.select(Person.class).list();
```

#### SQL-level queries

When making a SQL-level query, no prior registration steps are required. Specific columns can be selected
using the `litebridge.select(String...)` method, or all columns can be selected using `litebridge.select()`:

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

### Expression-level queries

Advanced queries can be constructed using "query expressions". These allow selecting SQL functions, aliased columns, 
or performing ORM-side type conversions. They work in both DTO-level and SQL-level contexts.

Expressions are primarily created using the `Fn` utility class. 
See the [Query Expressions](expressions.md) page for a full list of available expressions.

```java
import static org.litebridgedb.orm.expression.Fn.*;

// Select count of rows
Long count = litebridge.select(count()).from(Person.class).oneOrThrow();

// Select with SQL functions
List<Row> names = litebridge.select(upper("FIRST_NAME")).from("LB.PERSON").list();

// Select with ORM-side type conversion
Double avgAge = litebridge.select(convert(avg("age"), Double.class)).from(Person.class).oneOrThrow();
```

#### Terminal operations

Terminal operations are the last step in a query and define how results must be returned.

#### Get first result

The `first*` suite of terminals provides easy ways to get the first result from a query resulting in any number of records:

- `first()` returns an `Optional` with the first result if any rows were found, or an empty `Optional` otherwise
- `firstOrNull()` returns `null` if no rows were found, or the first result otherwise
- `firstOrThrow()` throws a `NoSuchElementException` if no rows were found
- `firstOrThrow(Supplier<Throwable>)` allows a custom exception to be thrown if no rows were found

#### Get exactly one result

The `one*` suite of terminals provides easy ways to get the result of a query resulting in exactly one record. 
They throw an `IllegalStateException` if multiple rows are returned from the database.

- `one()` returns an `Optional` with the single result if one matched, or an empty `Optional` otherwise
- `oneOrNull()` returns `null` if no rows were found, or the single result otherwise
- `oneOrThrow()` throws a `NoSuchElementException` if no rows were found
- `oneOrThrow(Supplier<Throwable>)` allows a custom exception to be thrown if no rows were found

#### All results

The remaining terminals provide various ways to iterate/retrieve all results from a query:

- `stream()` returns a `Stream` of the results
- `list()` returns a `List` containing all results
- `forEach(Consumer<T>)` iterates over the results and applies the given consumer to each row

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

### Many-to-Many JOINs

Many-to-many relationships can also be fetched using the same `join()` API:

```java
Group group = litebridge.select(Group.class)
        .join(Person.class).on("members")
        .where("name").eq("Administrators")
        .oneOrThrow();

// group.members contains the Person objects in this group
```

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
import static org.litebridgedb.orm.expression.Fn.*;

// Aggregates
litebridge.select(avg("age")).from(Person.class).oneOrThrow();
litebridge.select(min("age")).from(Person.class).oneOrThrow();
litebridge.select(max("age")).from(Person.class).oneOrThrow();
litebridge.select(count()).from(Person.class).oneOrThrow();

// String functions
litebridge.select(upper("name")).from(Person.class).list();
litebridge.select(lower("name")).from(Person.class).list();
litebridge.select(substring("name", 1, 3)).from(Person.class).list();

// Math functions
litebridge.select(abs("balance")).from(Account.class).list();
```

### ORM-side Type Conversion

Litebridge allows the Java type returned by an expression tob overridden using `Fn.convert()`. 
This uses Litebridge's registered type converters to transform the database result before it is returned to the client application.

This is particularly useful for aggregate functions where the database driver might return a different numeric type than what your application expects (e.g., getting a `Double` from an `AVG()` function when you want a `BigDecimal` or `Long`).

```java
// Convert the result of COUNT() to a String
String countStr = litebridge.select(Fn.convert(Fn.count(), String.class))
        .from(Person.class)
        .oneOrThrow();

// Convert AVG() result to a Double
Double avgAge = litebridge.select(Fn.convert(Fn.avg("age"), Double.class))
        .from(Person.class)
        .oneOrThrow();
```

### Column Aliasing

Column selections can be aliased, which is especially useful when selecting from multiple tables 
or when using expressions in "raw SQL" queries:

```java
import static org.litebridgedb.orm.expression.Fn.*;

litebridge.select(
    columnAlias("FIRST_NAME", "firstName"),
    columnAlias("SURNAME", "lastName")
).from("LB.PERSON").list();
```

When using `columnAlias()`, the resulting `Row` will contain the specified aliases as column names.