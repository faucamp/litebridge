# Manual Spring Configuration

← [Spring Integration](index.md)

For applications not using Spring Boot, or when complete control over bean lifecycle and configuration is required, Litebridge beans can be manually defined.

## Dependencies

Add the `litebridge-spring` and the chosen database provider to the `pom.xml`:

```xml

<dependency>
    <groupId>org.litebridge</groupId>
    <artifactId>litebridge-spring</artifactId>
    <version>0.3.0</version> <!-- Replace with latest version -->
</dependency>
<dependency>
<groupId>org.litebridge.db</groupId>
<artifactId>litebridge-db-h2</artifactId>
<version>0.3.0</version> <!-- Replace with latest version -->
</dependency>
```

## Configuration Class

Define the `LitebridgeTransactionManager` and `Litebridge` beans in a `@Configuration` class.

```java
import org.litebridge.orm.Litebridge;
import org.litebridge.spring.LitebridgeEntityScanner;
import org.litebridge.spring.LitebridgeTypeSafeDtoMappingScanner;
import org.litebridge.spring.LitebridgeTransactionManager;

@Configuration
@EnableTransactionManagement
public class LitebridgeConfig {

    @Bean
    public LitebridgeTransactionManager transactionManager(DataSource dataSource) {
        return new LitebridgeTransactionManager(dataSource);
    }

    @Bean
    public Litebridge litebridge(LitebridgeTransactionManager transactionManager) {
        // Select the database provider
        DatabaseProvider databaseProvider = new H2DatabaseProvider();

        Litebridge litebridge = new Litebridge(databaseProvider, transactionManager);

        // Register DTOs manually
        litebridge.register(User.class, rc -> rc.mapToTable("LB.USERS")
                .mapField("id").toColumn("ID")
                .mapField("username").toColumn("USERNAME"));

        // Or use Litebridge scanners for automatic entity registration
        Class<?>[] entities = new LitebridgeEntityScanner().scanBasePackage("com.example.app.entity");
        litebridge.register(entities);

        return litebridge;
    }
}
```

## Entity Scanning

If [entity annotations](../persistence/entity-annotations.md) are used, the `LitebridgeEntityScanner` can be used to automatically discover and register entities during configuration. Similarly, `LitebridgeTypeSafeDtoMappingScanner` can be used to discover implementations of `TypeSafeDtoTableMapping`.

```java
// Scan for @Table-annotated classes
Class<?>[] entities = new LitebridgeEntityScanner().scanBasePackage("com.example.app.entities");
litebridge.register(entities);

// Scan for TypeSafeDtoTableMapping implementations
TypeSafeDtoTableMapping[] mappings = new LitebridgeTypeSafeDtoMappingScanner().scanBasePackage("com.example.app.mappings");
litebridge.register(mappings);
```

These classes leverage Spring's `ClassPathScanningCandidateComponentProvider` to find relevant components in the specified packages.

## Key Considerations

### Transaction Management

Litebridge uses its own `LitebridgeTransactionManager`, which implements Spring's `PlatformTransactionManager`. This allows it to participate in Spring-managed transactions. Ensure the configuration includes `@EnableTransactionManagement`.

### Bean Dependencies

The `Litebridge` bean depends on the `LitebridgeTransactionManager`. When using Spring Boot's database initialization (e.g., `schema.sql`), `@DependsOnDatabaseInitialization` may be required to ensure the database is ready before Litebridge attempts to register mappings or interact with it.

```java
@Bean
@DependsOnDatabaseInitialization
public Litebridge litebridge(LitebridgeTransactionManager transactionManager) {
    // ...
}
```
