# Change Tracking

← [Persistence](index.md)

Litebridge includes a built-in change tracking mechanism that helps optimise database updates by only persisting fields that have actually changed.

Purpose:
- **Performance**: Reduced database load by only updating changed columns.
- **Efficiency**: No need to manually keep track of what changed in application logic.
- **Safety**: Minimises the risk of overwriting concurrent changes to unrelated columns (depending on the database's locking strategy).

Change tracking works by wrapping a DTO in a proxy that intercepts calls to its setters (or by comparing the current state with a snapshot of the original state). When a tracked DTO is saved, Litebridge only generates SQL for the modified fields.

## Usage

### Enabling change tracking

To enable change tracking for a DTO, use the `litebridge.track()` method:

```java
Person person = litebridge.track(new Person());
person.setName("Alice");
person.setSurname("Smith");

// The first save will perform an INSERT
litebridge.save(person);

// Subsequent changes are tracked
person.setAge(21);

// This save will perform an UPDATE, only for the 'AGE' column
litebridge.save(person);
```

### Cascading and tracking

When tracking a DTO that has relationships, Litebridge can also track the related DTOs if they are properly configured.

```java
Person person = litebridge.track(new Person());
Account account = litebridge.track(new Account());
account.setOwner(person);

// Saving the account will also save the person due to cascading
litebridge.save(account);
```

## Implicit tracking

When DTOs are retrieved via a `select()` query, they are automatically tracked by Litebridge. This means any changes made to them can be persisted simply by calling `save()`.

```java
Person person = litebridge.select(Person.class)
    .where("id").eq(1L)
    .oneOrThrow();

person.setEyeColour("Green");

// Only EYE_COLOUR will be updated in the database
litebridge.save(person);
```
