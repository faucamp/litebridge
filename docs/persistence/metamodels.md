# Metamodels

← [Persistence](index.md)

Metamodels provide a type-safe way to reference DTO/entity fields in Litebridge queries. 
Instead of using strings to refer to fields (which are prone to typos and don't survive refactorings), static constants from a generated or manually created metamodel class are used.

## Overview

A metamodel is a utility class that describes the structure of a DTO or entity. 
For every field in a DTO, the metamodel has a corresponding public static final field of type `QueryField` (or one of its specialised subclasses like `StringQueryField` or `NumericQueryField`).

### Benefits
- **Type Safety**: Errors are caught at compile-time instead of runtime.
- **IDE Support**: Autocomplete works for field names and available SQL functions.
- **Refactoring**: Renaming a field in a DTO (and updating the metamodel) will automatically update all references in queries.

## Structure

A typical metamodel class looks like this:

```java
// Metamodel for the Person DTO/entity class
public class PersonMeta {
    public static final StringQueryField id = new StringQueryField(Person.class, "id");
    public static final StringQueryField firstName = new StringQueryField(Person.class, "firstName");
    public static final NumericQueryField age = new NumericQueryField(Person.class, "age");
    public static final QueryField accounts = new QueryField(Person.class, "accounts");
}
```

### QueryField Types
- `QueryField`: The base class for all metamodel fields.
- `StringQueryField`: Provides string-specific SQL functions like `.upper()` and `.lower()`.
- `NumericQueryField`: Used for numeric fields. Provides numeric-specific SQL functions like `max()`.

## Usage in Queries

Metamodels can be used in almost all parts of the Litebridge fluent API.

### Selecting Specific Fields

Metamodel fields can be used in the `select()` method to retrieve only specific data:

```java
import static org.example.meta.PersonMeta.*;

// Selec the ID and uppercase first name and populate that in a Person object
List<Person> results = litebridge.select(id, firstName.upper())
    .from(Person.class)
    .list();
```

### Where Clauses

Metamodels are most powerful when used in `where()` clauses for filtering:

```java
import static org.example.meta.PersonMeta.*;

Person person = litebridge.select(Person.class)
    .where(firstName).eq("Alice")
    .and(age).gte(30)
    .oneOrThrow();
```

### Joining Tables

Relation fields in metamodels can be used to define joins between tables:

```java
import static org.example.meta.PersonMeta.*;

Person person = litebridge.select(Person.class)
    .join(Account.class).on(accounts) // Uses the 'accounts' relation field
    .where(id).eq(123L)
    .oneOrThrow();
```

### Ordering Results

Metamodels make sorting type-safe as well:

```java
import static org.example.meta.PersonMeta.*;

List<Person> users = litebridge.select(Person.class)
    .orderBy(firstName).asc()
    .andBy(age).desc()
    .list();
```

### Update and Delete Operations

Metamodels can also be used in the [update](update.md) and [delete](delete.md) APIs:

```java
import static org.example.meta.PersonMeta.*;

// Type-safe update
litebridge.update(Person.class, p -> p
        .set(eyeColour).to("green")
        .where(name).eq("Alice"));

// Type-safe delete
litebridge.delete(Person.class, p -> p
        .where(name).eq("Henry")
        .and(age).eq(45));
```

## Creating Metamodels

### Using the Maven Plugin (Recommended)

The easiest way to create metamodels is by using the `litebridge-maven-plugin`. It can automatically generate metamodel classes for all registered entities and DTOs.

See the [Maven metamodel goal](../maven/metamodel.md) documentation for configuration details.

### Manual Creation

If manual creation is preferred over the Maven plugin, metamodel classes can be created manually. Simply create a class with public static final `QueryField` instances:

```java
public class MyDtoMeta {
    public static final StringQueryField name = new StringQueryField(MyDto.class, "name");
    public static final NumericQueryField count = new NumericQueryField(MyDto.class, "count");
}
```

Ensure that the field name passed to the constructor matches the field name in the DTO class.
The first parameter of the `QueryField` constructor is the tareget entity/DTO class, follwed by the entity/DTO's field name.
