# Configuration
  
← [Persistence](index.md)

Litebridge can be configured using the `LitebridgeConfig` class. This configuration allows global behavior for the ORM to be defined, such as how related DTOs are handled.

## LitebridgeConfig

The `LitebridgeConfig` class contains various settings that can be passed to the `Litebridge` instance during construction.

### Related DTO Strategy

The `RelatedDtoStrategy` determines how Litebridge handles related DTO fields when they are not explicitly included in a `JOIN` clause in a query.

There are two available strategies:

1. **`NULL_IF_NO_JOIN` (Default)**: Related DTO fields will be set to `null` if they are not joined in the query.
2. **`PARTIAL_OBJECT_IF_NO_JOIN`**: Related DTO fields will be partially populated with an instance of the related DTO containing only its primary key(s), even if not joined.

#### Example: PARTIAL_OBJECT_IF_NO_JOIN

Given a `Person` and `Account` relationship:

```java
public class Person {
    private Long id;
    private String name;
    // ...
}

public class Account {
    private Long id;
    private String name;
    private Person owner; // Related DTO
    // ...
}
```

If `PARTIAL_OBJECT_IF_NO_JOIN` is configured:

```java
// Global configuration
LitebridgeConfig config = new LitebridgeConfig();
config.setRelatedDtoStrategy(RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN);

Litebridge litebridge = new Litebridge(databaseProvider, transactionManager, config);

// Query without explicit join
Account account = litebridge.select(Account.class)
    .where("id").eq(1L)
    .oneOrThrow();

// account.getOwner() will NOT be null. 
// It will contain a Person instance where only the ID is set.
Long ownerId = account.getOwner().getId(); 
String ownerName = account.getOwner().getName(); // This will be null
```

This strategy is useful when the ID of a related object is required (e.g., for creating links or further queries) without incurring the overhead of a SQL `JOIN`.

## Setup with Configuration

To apply the configuration, pass it to the `Litebridge` constructor:

```java
LitebridgeConfig config = new LitebridgeConfig();
config.setRelatedDtoStrategy(RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN);

Litebridge litebridge = new Litebridge(databaseProvider, dataSource, config);
```

For Spring-based applications, it can be defined as a bean:

```java
@Bean
public Litebridge litebridge(LitebridgeTransactionManager transactionManager) {
    LitebridgeConfig config = new LitebridgeConfig();
    config.setRelatedDtoStrategy(RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN);
    
    return new Litebridge(new H2DatabaseProvider(), transactionManager, config);
}
```
