# Spring Boot Starter

← [Spring Integration](index.md)

The `litebridge-spring-boot-starter` provides a convenient way to integrate Litebridge into your Spring Boot application with minimal configuration.

## Dependency

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.litebridgedb</groupId>
    <artifactId>litebridge-spring-boot-starter</artifactId>
    <version>0.3.0</version> <!-- Replace with latest version -->
</dependency>
```

## Autoconfiguration

When the starter is on the classpath, Litebridge will automatically:

1.  **Detect a `DatabaseProvider`**: It scans the classpath for implementations of `DatabaseProvider`. If exactly one is found, it's used.
2.  **Configure `LitebridgeTransactionManager`**: It creates a transaction manager that uses your application's `DataSource`.
3.  **Create the `Litebridge` bean**: It instantiates the main `Litebridge` engine, ready for injection.

## Configuration Properties

You can customise the autoconfiguration using the following properties in `application.properties` or `application.yml`:

| Property | Description | Default |
| :--- | :--- | :--- |
| `litebridge.database-provider.class` | Fully qualified class name of the `DatabaseProvider` to use. | (Auto-detected) |
| `litebridge.database-provider.scan-base-package` | Base package to scan for `DatabaseProvider` implementations if `class` is not set. | `org.litebridgedb.db` |
| `litebridge.scan-base-package` | One or more base packages to scan for Litebridge entities (annotated with `@Table`) and `TypeSafeDtoTableMapping` implementations. | (None) |

### Example

```properties
litebridge.database-provider.class=org.litebridgedb.db.h2.H2DatabaseProvider
litebridge.scan-base-package=com.example.app.entities,com.example.app.mappings
```

## Entity and Mapping Registration

While you can manually register your DTO-to-table mappings, the starter supports automatic discovery via the `litebridge.scan-base-package` property
if you are using [entity annotations](../persistence/entity-annotations.md) or type-safe mappings.

### Automatic Scanning

When `litebridge.scan-base-package` is configured, Litebridge will automatically:

1.  **Scan for Entities**: Find classes annotated with `@Table` using `LitebridgeEntityScanner`.
2.  **Scan for Type-Safe Mappings**: Find implementations of `TypeSafeDtoTableMapping` using `LitebridgeTypeSafeDtoMappingScanner`.

These will be registered automatically during the initialisation of the `Litebridge` bean.

### Manual Registration

You can still register mappings manually in a `@Configuration` class or during application startup.

```java
@Configuration
public class MyLitebridgeConfig {

    @Autowired
    public void registerMappings(Litebridge litebridge) {
        litebridge.register(User.class, TableSpec.builder("users")
            .id("id", User::id)
            .column("username", User::username)
            .build());
    }
}
```

## Usage

Once configured, you can inject `Litebridge` into your services and use it alongside Spring's `@Transactional`.

```java
@Service
public class UserService {

    private final Litebridge litebridge;

    public UserService(Litebridge litebridge) {
        this.litebridge = litebridge;
    }

    @Transactional
    public void createUser(User user) {
        litebridge.save(user);
    }
}
```
