# Entity Annotations

← [Home](../index.md) | [DTO-table mapping](dto-table-mapping.md)

Litebridge provides an annotation-based approach for mapping DTOs to database tables. This "entity-style" registration 
allows the definition of mappings directly on classes using annotations, making the configuration 
more concise and co-located with the data structure.

## Dependencies

To use Litebridge annotations, the `litebridge-annotations` module must be included in the project:

```xml

<dependency>
    <groupId>org.litebridge</groupId>
    <artifactId>litebridge-annotations</artifactId>
    <version>${litebridge.version}</version>
</dependency>
```

Note: this dependency is not required if the `litebridge-orm` module is already included in the module containing 
the entity classes.

And update `module-info.java` if JPMS is used:

```java
module my.module {
    requires org.litebridge.orm;
    requires org.litebridge.orm.annotation;
}
```

## Basic Mapping

To register a class using annotations, use the `@Table` and `@Column` annotations:

```java
import org.litebridge.orm.annotation.Column;
import org.litebridge.orm.annotation.Table;

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

Once an entity is annotated, it can be registered with Litebridge:

```java
// Register using current lookup
litebridge.register(Person.class);

// Or register providing a specific lookup (required for accessing private members in other modules)
litebridge.register(MethodHandles.lookup(), Person.class);
```

Multiple entities can also be registered at once, which is particularly useful if they refer to each other:

```java
litebridge.register(Person.class, Account.class);
```

### Package Scanning

The `litebridge-orm-support` module allows scanning packages for entity classes and register them automatically.

#### Dependencies

Include the `litebridge-orm-support` module:

```xml

<dependency>
    <groupId>org.litebridge</groupId>
    <artifactId>litebridge-orm-support</artifactId>
    <version>${litebridge.version}</version>
</dependency>
```

And update `module-info.java` (if applicable):

```java
module my.module {
    requires org.litebridge.orm;
    requires org.litebridge.orm.support;
}
```

#### Usage

Use `TypesafeRegistrationSupport` to scan one or more packages:

```java
import org.litebridge.orm.support.EntityScanner;

// Create the scanner with the Litebridge instance
EntityScanner scanner = new EntityScanner(litebridge);

// Scan and register all entity classes in the specified packages
scanner.

        scanBasePackage("com.example.app.mappings");
```

### Spring Integration

In Spring applications, the `LitebridgeEntityScanner` can be used to automatically discover and register annotated entities. See [Spring Manual Configuration](../spring/manual-configuration.md#entity-and-mapping-scanning) and [Spring Boot Starter](../spring/spring-boot-starter.md#entity-and-mapping-registration) for more details.

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

If there are many annotated entities, manually registering each one can be tedious. The `litebridge-orm-support` module provides the `EntityPackageRegistrationSupport` class to scan packages for classes annotated with `@Table` and register them automatically.

### Dependencies

To use the registration support, include the `litebridge-orm-support` module:

```xml

<dependency>
    <groupId>org.litebridge</groupId>
    <artifactId>litebridge-orm-support</artifactId>
    <version>${litebridge.version}</version>
</dependency>
```

And update `module-info.java`:

```java
module my.module {
    requires org.litebridge.orm;
    requires org.litebridge.orm.support;
}
```

### Usage

Use `EntityPackageRegistrationSupport` to scan one or more packages:

```java
import org.litebridge.orm.support.EntityScanner;

// Create the scanner with the Litebridge instance
EntityPackageRegistrationSupport scanner = new EntityPackageRegistrationSupport(litebridge);

// Scan and register all @Table annotated classes in the specified packages
scanner.

        scanBasePackage("com.example.app.entities","com.example.app.other.entities");
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
