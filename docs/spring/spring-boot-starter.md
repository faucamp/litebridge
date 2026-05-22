# Spring Boot Starter

← [Spring Integration](index.md)

The `litebridge-spring-boot-starter` provides a convenient way to integrate Litebridge into your Spring Boot application with minimal configuration.

## Dependency

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.litebridgedb</groupId>
    <artifactId>litebridge-spring-boot-starter</artifactId>
    <version>0.1.3</version> <!-- Replace with latest version -->
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

### Example

```properties
litebridge.database-provider.class=org.litebridgedb.db.h2.H2DatabaseProvider
```

## DTO Registration

While Litebridge is autoconfigured, you still need to register your DTO-to-table mappings. You can do this in a `@Configuration` class or during application startup.

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
