# Merging data

← [Persistence](index.md)

Litebridge supports the SQL `MERGE` statement (also known as "upsert") via the `mergeInto()` API. This allows for conditionally performing `INSERT`, `UPDATE`, or `DELETE` operations on a target table based on the results of a join with a source DTO or table.

Similar to other mutating query operations, `mergeInto()` returns an `UpdateResult` containing information such as the number of rows affected.

## DTO-level merge

```java
import static org.example.meta.AccountMeta.*;
import static org.example.meta.PersonMeta.*;

UpdateResult result = litebridge.mergeInto(Account.class, m -> m
        .using(Person.class)
        .on(AccountMeta.id, PersonMeta.id)
        .whenMatched(matched -> matched
                .update(u -> u.set(balance).to(500))
                .where(AccountMeta.id.lt(5)))
        .whenMatched(matched -> matched
                .delete())
        .whenNotMatched(notMatched -> notMatched
                .insert(i -> i
                        .set(accountId).to(123L)
                        .set(accountName).to("Default Account")
                        .set(balance).to(0))));
```

The `mergeInto()` API provides a fluent builder to define:
- `using()`: The source DTO class or table name.
- `on()`: The join condition between the target and source.
- `whenMatched()`: Operations to perform when a match is found (update or delete). An optional `where()` clause can be provided to further filter matched rows.
- `whenNotMatched()`: The `INSERT` operation to perform when no match is found.

## SQL-level merge

SQL-level merge operations are also supported:

```java
litebridge.mergeInto("LB.ACCOUNT", m -> m
        .using("LB.PERSON")
        .on("LB.ACCOUNT.PERSON_ID", "LB.PERSON.ID")
        .whenMatched(matched -> matched
                .update(u -> u.set("BALANCE").to(500)))
        .whenNotMatched(notMatched -> notMatched
                .insert(i -> i
                        .set("ACCOUNT_ID").to(123L)
                        .set("BALANCE").to(0))));
```
