# Transactions

← [Persistence](index.md)

Litebridge provides a programmatic API for transaction management, giving full control over when transactions start, 
commit, or rollback. It supports manual control, try-with-resources for automatic resource management, and lambda-based 
execution for a more functional approach.

## Starting a transaction

Transactions are accessed via the `litebridge.transaction()` method. This returns a `TransactionContext` which allows 
the configuration and beginning of a transaction.

## Transaction management patterns

### Manual control

The lifecycle of a transaction can be manually controlled using `begin()`, `commit()`, and `rollback()`. 

```java
litebridge.transaction().begin();
try {
    litebridge.save(person);
    litebridge.transaction().commit();
} catch (Exception e) {
    litebridge.transaction().rollback();
    throw e;
}
```

### Try-with-resources

The `begin()` method returns a `Transaction` object that implements `AutoCloseable`. If the transaction is not explicitly 
committed before the block exits, it will be automatically rolled back when `close()` is called.

```java
try (Transaction tx = litebridge.transaction().begin()) {
    litebridge.save(person);
    tx.commit();
}
```

This pattern ensures that transactions are always closed and properly rolled back in case of 
unexpected exceptions (no explicit `rollback()` required), but it requires an explicit `commit()` call to signal 
that everything completed as intended.

### Lambda execution

For a more concise approach, the `execute()` method can be used, which handles the transaction lifecycle. 
If the lambda completes successfully, the transaction is committed. If an exception is thrown, it is automatically rolled back.

```java
litebridge.transaction().execute(() -> {
    litebridge.save(person);
    litebridge.save(account);
});
```

Note that `execute()` wraps any checked exceptions into a `TransactionException`.

This is the recommended approach for most use cases as it handles commit/rollback automatically.

## Transaction configuration

Before calling `begin()` or `execute()`, the transaction can be configured with specific attributes.

### Read-only transactions

Indicating a transaction is read-only can allow the database to optimise performance and prevent accidental modifications.

```java
litebridge.transaction()
    .readOnly()
    .execute(() -> {
        List<Person> people = litebridge.select(Person.class).list();
        // ...
    });
```

### Isolation levels

The isolation level for the transaction can be specified using the `isolation()` method.

```java
import org.litebridgedb.db.spi.tx.Isolation;

litebridge.transaction()
    .isolation(Isolation.SERIALIZABLE)
    .execute(() -> {
        // ...
    });
```

Available isolation levels depend on the `DatabaseProvider` but generally include:
- `DEFAULT`
- `READ_UNCOMMITTED`
- `READ_COMMITTED`
- `REPEATABLE_READ`
- `SERIALIZABLE`

## DTO state rollback

A unique feature of Litebridge is that when a transaction is rolled back, it also attempts to roll back the in-memory 
state of any DTOs that were tracked during the transaction.

For example, if an ID was assigned to a DTO during a `save()` operation, and the transaction is subsequently rolled back, 
the ID will be restored to its previous value (usually `null`). This also applies to tracked collections and other properties.

```java
try (Transaction tx = litebridge.transaction().begin()) {
    litebridge.save(person);
    System.out.println(person.getId()); // ID is set here
    tx.rollback();
}

System.out.println(person.getId()); // ID is null again
```

## Nested transactions

Litebridge supports nested transactions. Calling `begin()` while a transaction is already active will increment a 
nesting level. The actual database transaction is only committed when the outermost transaction is committed. 
A rollback at any level will mark the entire transaction for rollback.
