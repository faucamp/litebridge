# Entity Annotations

← [Home](../index.md) | [DTO-table mapping](dto-table-mapping.md)

Litebridge provides an annotation-based approach for mapping DTOs to database tables. This "entity-style" registration allows you to define mappings directly on your classes using annotations, making the configuration more concise and co-located with the data structure.

## Dependencies

To use Litebridge annotations, you need to include the `litebridge-annotations` module in your project:

```xml
<dependency>
    <groupId>org.litebridgedb</groupId>
    <artifactId>litebridge-annotations</artifactId>
    <version>${litebridge.version}</version>
</dependency>
```

Note: this dependency is not required if you are already including the `litebridge-orm` module the module containing 
your entity classes.

And update your `module-info.java` if you are using JPMS:

```java
module my.module {
    requires org.litebridgedb.orm;
    requires org.litebridgedb.orm.annotation;
}
```

## Basic Mapping

To register a class using annotations, use the `@Table` and `@Column` annotations:

```java
import org.litebridgedb.orm.annotation.Column;
import org.litebridgedb.orm.annotation.Table;

@Table("LB.PERSON")
public class Person {
    @Column(value = "PERSON_ID", generateUsingSequence = "LB.PERSON_SEQ")
    private Long id;

    @Column("FIRST_NAME")
    private String name;

    @Column("SURNAME")
    private String surname;

    @Column("AGE")
    private int age;

    private String eyeColour;

    // Getters and setters...

    @Column("EYE_COLOUR")
    public String getEyeColour() {
        return eyeColour;
    }
}
```

### Registration

Once your entity is annotated, you can register it with Litebridge:

```java
// Register using current lookup
litebridge.register(Person.class);

// Or register providing a specific lookup (required for accessing private members in other modules)
litebridge.register(MethodHandles.lookup(), Person.class);
```

You can also register multiple entities at once, which is particularly useful if they refer to each other:

```java
litebridge.register(Person.class, Account.class);
```

## Relationships

Litebridge annotations support one-to-many and many-to-many relationships.

### One-to-Many

The `@OneToMany` annotation is used to define the "many" side of a relationship from the "one" side. It requires the `mappedByField` attribute, which points to the field in the target entity that defines the relationship.

```java
@Table("LB.PERSON")
public class Person {
    // ... other fields

    @OneToMany(mappedByField = "owner")
    private List<Account> accounts;
}

@Table("LB.ACCOUNT")
public class Account {
    @Column("ACCOUNT_ID")
    private Long id;

    @Column(value = "PERSON_ID", joinUsing = true)
    private Person owner;
}
```

In the example above:
- `Account.owner` is mapped to the `PERSON_ID` column. `joinUsing = true` indicates that it should use a `JOIN USING (PERSON_ID)` or equivalent join condition.
- `Person.accounts` is a virtual collection populated by Litebridge based on the `owner` field in `Account`.

### Many-to-Many

The `@ManyToMany` annotation defines a relationship via a join table.

```java
@Table("LB.GROUP")
public class Group {
    @Column("GROUP_NAME")
    private String name;

    @ManyToMany(
        joinTable = "LB.PERSON_GROUP", 
        joinColumn = "GROUP_NAME", 
        inverseJoinColumn = "PERSON_ID"
    )
    private List<Person> members;
}
```

- `joinTable`: The name of the intermediate table.
- `joinColumn`: The column in the join table referencing the current entity (`Group`).
- `inverseJoinColumn`: The column in the join table referencing the target entity (`Person`).

### Handling Interfaces and Base Classes (`@AllowInterface`)

Litebridge does not automatically assume that a base class or interface can be used as a stand-in for a specific registered entity, as there could be multiple entities implementing the same interface.

To allow an entity to be referenced via its interface or base class in relationships (One-to-One, One-to-Many, or Many-to-Many), use the `@AllowInterface` annotation on the entity class.

```java
@Table("LB.PERSON")
@AllowInterface(Person.class) // Allows using 'Person' as a reference to 'GroupedPerson'
public class GroupedPerson extends Person {
    // ...
}

@Table("LB.GROUP")
public class Group {
    @ManyToMany(...)
    private List<Person> members; // Litebridge now knows it can use 'GroupedPerson' here
}
```

## Package Scanning

If you have many annotated entities, manually registering each one can be tedious. The `litebridge-orm-support` module provides the `EntityPackageRegistrationSupport` class to scan packages for classes annotated with `@Table` and register them automatically.

### Dependencies

To use the registration support, include the `litebridge-orm-support` module:

```xml
<dependency>
    <groupId>org.litebridgedb</groupId>
    <artifactId>litebridge-orm-support</artifactId>
    <version>${litebridge.version}</version>
</dependency>
```

And update your `module-info.java`:

```java
module my.module {
    requires org.litebridgedb.orm;
    requires org.litebridgedb.orm.support;
}
```

### Usage

Use `EntityPackageRegistrationSupport` to scan one or more packages:

```java
import org.litebridgedb.orm.support.EntityPackageRegistrationSupport;

// Create the scanner with your Litebridge instance
EntityPackageRegistrationSupport scanner = new EntityPackageRegistrationSupport(litebridge);

// Scan and register all @Table annotated classes in the specified packages
scanner.scanBasePackage("com.example.app.entities", "com.example.app.other.entities");
```

## Annotation Reference

### `@Table`
Applied to the class level to specify the target database table.
- `value`: The name of the table (e.g., `"LB.PERSON"`).

### `@AllowInterface`
Applied to the class level. Specifies interfaces or base classes that should be recognized as this entity type when used in relationships.
- `value`: An array of `Class` objects.

### `@Column`
Applied to fields or getter methods.
- `value`: The database column name.
- `joinOn`: Custom SQL join condition (e.g., `"T1.ID = T2.PARENT_ID"`).
- `joinUsing`: Boolean. If `true`, uses the column name for a `JOIN USING` clause.
- `generator`: Specifies a `ColumnValueGenerator` class for dynamic value generation.
- `generateUsingSequence`: Specifies a database sequence name for value generation (e.g., for primary keys).

### `@OneToMany`
Applied to a collection field or method.
- `mappedByField`: The name of the field in the target entity that owns the relationship.

### `@ManyToMany`
Applied to a collection field or method.
- `joinTable`: The join table name.
- `joinColumn`: The column in the join table referencing the current entity.
- `inverseJoinColumn`: The column in the join table referencing the target entity.
