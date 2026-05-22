# Manual Spring Configuration

← [Spring Integration](index.md)

For applications not using Spring Boot, or when you need complete control over bean lifecycle and configuration, you can manually define Litebridge beans.

## Dependencies

Add the `litebridge-spring` and your chosen database provider to your `pom.xml`:

```xml
<dependency>
    <groupId>org.litebridgedb</groupId>
    <artifactId>litebridge-spring</artifactId>
    <version>0.2.0</version> <!-- Replace with latest version -->
</dependency>
<dependency>
    <groupId>org.litebridgedb</groupId>
    <artifactId>litebridge-db-h2</artifactId>
    <version>0.2.0</version> <!-- Replace with latest version -->
</dependency>
```

## Configuration Class

Define the `LitebridgeTransactionManager` and `Litebridge` beans in a `@Configuration` class.

```java
@Configuration
@EnableTransactionManagement
public class LitebridgeConfig {

    @Bean
    public LitebridgeTransactionManager transactionManager(DataSource dataSource) {
        return new LitebridgeTransactionManager(dataSource);
    }

    @Bean
    public Litebridge litebridge(LitebridgeTransactionManager transactionManager) {
        // Choose your database provider
        DatabaseProvider databaseProvider = new H2DatabaseProvider();
        
        Litebridge litebridge = new Litebridge(databaseProvider, transactionManager);

        // Register your DTOs
        litebridge.register(User.class, rc -> rc.mapToTable("LB.USERS")
                .mapField("id").toColumn("ID")
                .mapField("username").toColumn("USERNAME"));

        return litebridge;
    }
}
```

## Key Considerations

### Transaction Management

Litebridge uses its own `LitebridgeTransactionManager`, which implements Spring's `PlatformTransactionManager`. This allows it to participate in Spring-managed transactions. Ensure your configuration includes `@EnableTransactionManagement`.

### Bean Dependencies

The `Litebridge` bean depends on the `LitebridgeTransactionManager`. When using Spring Boot's database initialization (e.g., `schema.sql`), you may need to use `@DependsOnDatabaseInitialization` to ensure the database is ready before Litebridge attempts to register mappings or interact with it.

```java
@Bean
@DependsOnDatabaseInitialization
public Litebridge litebridge(LitebridgeTransactionManager transactionManager) {
    // ...
}
```
