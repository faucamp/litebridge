# Spring Boot Starter

← [Spring Integration](index.md)

The `litebridge-spring-boot-starter` provides a convenient way to integrate Litebridge into a Spring Boot application with minimal configuration.

## Dependency

Add the following dependency to the `pom.xml`:

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
2.  **Configure `LitebridgeTransactionManager`**: It creates a transaction manager that uses the application's `DataSource`.
3.  **Create the `Litebridge` bean**: It instantiates the main `Litebridge` engine, ready for injection.
4.  **Scan packages for entities**: It optionally scans a set of packages for entity classes and automatially registers them with Litebridge.

## Configuration Properties

The autoconfiguration can be customised using the following properties in `application.properties` or `application.yml`:

| Property                                         | Description                                                                                                                                                              | Default               |
|:-------------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------|
| `litebridge.database-provider.class`             | Fully qualified class name of the `DatabaseProvider` to use.                                                                                                             | (Auto-detected)       |
| `litebridge.database-provider.scan-base-package` | Base package(s) to scan for `DatabaseProvider` implementations if `class` is not set.                                                                                    | `org.litebridgedb.db` |
| `litebridge.scan-base-package`                   | One or more base packages to scan for Litebridge entities (annotated with `@Table`) and `TypeSafeDtoTableMapping` implementations.                                       | (None)                |
| `litebridge.related-dto-strategy`                | How related DTOs should be handled when not included as a JOIN in a query. See [Related DTO Strategy](../persistence/configuration.md#related-dto-strategy) for details. | `NULL_IF_NO_JOIN`     |

### Example

```properties
litebridge.database-provider.class=org.litebridgedb.db.h2.H2DatabaseProvider
litebridge.scan-base-package=com.example.app.entities,com.example.app.mappings
litebridge.related-dto-strategy=PARTIAL_OBJECT_IF_NO_JOIN
```

## Entity and Mapping Registration

While DTO-to-table mappings can be manually registered, the starter supports automatic discovery via the `litebridge.scan-base-package` property
if [entity annotations](../persistence/entity-annotations.md) are used.

### Automatic Scanning

When `litebridge.scan-base-package` is configured, Litebridge will automatically:

1.  **Scan for Entities**: Find classes annotated with `@Table` using `LitebridgeEntityScanner`.
2.  **Scan for Type-Safe Mappings**: Find implementations of `TypeSafeDtoTableMapping` using `LitebridgeTypeSafeDtoMappingScanner`.

These will be registered automatically during the initialisation of the `Litebridge` bean.

### Manual Registration

Mappings can still be registered manually in a `@Configuration` class or during application startup.

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

Once configured, `Litebridge` can be injected into services and used alongside Spring's `@Transactional`.

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
