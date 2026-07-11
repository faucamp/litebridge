# Deleting data

← [Persistence](index.md)

Litebridge provides a fluent API for deleting data, which allows for:

* DTO-level deletes
  * direct deletion of DTOs 
  * deletion via queries
* SQL-level deletes
* **Native SQL** deletes via raw SQL strings

## Deleting DTOs

### Deleting DTOs directly

Litebridge provides a simple mechanism for deleting objects, which functions similar to `save()`.
To delete an object, simply call `delete()` and pass the object as a parameter:

```java
Person person = new Person();
person.setId(1);

litebridge.delete(person);
```

### Deleting DTOs via a query

Litebridge also provides a fluent API for deleting registered object types via a query.
This is useful for deleting multiple objects that match certain criteria.
To delete objects via a query, use the `delete()` method with a DTO class and optional query lambda:

```java
litebridge.delete(Person.class, p -> p.where("age").gt(20));
```

The use of a lambda for the query avoids the need for an explicit terminal "execute" call.

#### Metamodel-based deletes

[Metamodels](metamodels.md) can also be used for type-safe deletions:

```java
import static org.example.meta.PersonMeta.*;

litebridge.delete(Person.class, p -> p
        .where(name).eq("Henry")
        .and(age).eq(45));
```

### Deleting all records

To delete all records for a registered DTO type, simply call `delete()` with the DTO class:

```java
litebridge.delete(Person.class);
```

## SQL-level deletes

### Deleting records via a query

Litebridge's fluent API allows for arbitrary SQL-level deletes:

```java
litebridge.delete("LB.PERSON", p -> p.where("AGE").gt(20));
```

For SQL-level deletes, no DTO registration is required.

### Deleting all rows from a table

To delete all rows from a table without specifying a query:

```java
litebridge.delete("LB.PERSON");
```

> [!NOTE]
> To execute raw SQL `DELETE` strings without using the fluent API, see [Native SQL Execution](native-sql.md).