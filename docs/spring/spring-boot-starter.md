# Spring Boot Starter

← [Spring Integration](index.md)

The `litebridge-spring-boot-starter` provides a convenient way to integrate Litebridge into a Spring Boot application with minimal configuration.

## Dependency

Add the following dependency to the `pom.xml`:

```xml

<dependency>
    <groupId>org.litebridge</groupId>
    <artifactId>litebridge-spring-boot-starter</artifactId>
    <version>0.4.0</version> <!-- Replace with latest version -->
</dependency>
```

## Autoconfiguration

When the starter is on the classpath, Litebridge will automatically:

1.  **Detect a `DatabaseProvider`**: It scans the classpath for implementations of `DatabaseProvider`. If exactly one is found, it's used.
2.  **Configure `LitebridgeTransactionManager`**: It creates a transaction manager that uses the application's `DataSource`.
3.  **Create the `Litebridge` bean**: It instantiates the main `Litebridge` engine, ready for injection.
4.  **Auto-configure Repositories**: It automatically discovers and registers interfaces extending `LitebridgeRepository` found in the application's package hierarchy.
5.  **Scan packages for entities**: It optionally scans a set of packages for entity classes and automatically registers them with Litebridge.

## Configuration Properties

The autoconfiguration can be customised using the following properties in `application.properties` or `application.yml`:

| Property                                     | Description                                                                                                                                                              | Default             |
|:---------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------|
| `litebridge.database-provider.class`         | Fully qualified class name of the `DatabaseProvider` to use.                                                                                                             | (Auto-detected)     |
| `litebridge.database-provider.scan-base-package` | Base package(s) to scan for `DatabaseProvider` implementations if `class` is not set.                                                                                | `org.litebridge.db` |
| `litebridge.scan-base-package`               | One or more base packages to scan for Litebridge entities (annotated with `@Table`) and `TypeSafeDtoTableMapping` implementations.                                       | (None)              |
| `litebridge.related-dto-strategy`            | How related DTOs should be handled when not included as a JOIN in a query. See [Related DTO Strategy](../persistence/configuration.md#related-dto-strategy) for details. | `NULL_IF_NO_JOIN`   |
| `litebridge.repositories.enabled`| Controls whether Litebridge Spring Data repositories are automatically registered.                                                                                       | `true`              |

### Example

```properties
litebridge.database-provider.class=org.litebridge.db.h2.H2DatabaseProvider
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

Once configured, `Litebridge` and repository beans can be injected into services and used alongside Spring's `@Transactional`.

### Using Litebridge Directly

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

### Spring Data Repositories

Any interface extending `LitebridgeRepository` located within the application's base package (or packages registered with `@AutoConfigurationPackage`) is automatically registered as a Spring bean:

```java
public interface UserRepository extends LitebridgeRepository<User, Long> {
}
```

The repository can be directly injected into services:

```java
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
```

#### Derived Query Methods

`LitebridgeRepository` supports Spring Data's Derived Query Methods via `LitebridgePartTreeQuery`. 
Query criteria, logical operators, projections, sorting, and limits are parsed directly from repository method names and converted into Litebridge AST queries.

##### Supported Keywords

The following keywords and operators are supported in method names:

| Keyword | Part Type | SQL Operation | Example Method Signature|
|:---|:---|:---|:---|
| `Is`, `Equals`, (none) | Simple Property | `=` | `findByName(String name)` |
| `IsNot`, `Not` | Negating Simple Property | `<>` | `findByNameIsNot(String name)` |
| `GreaterThan` | Greater Than | `>` | `findByAgeGreaterThan(int age)` |
| `GreaterThanEqual` | Greater Than Equal | `>=` | `findByAgeGreaterThanEqual(int age)` |
| `LessThan` | Less Than | `<` | `findByAgeLessThan(int age)` |
| `LessThanEqual` | Less Than Equal | `<=` | `findByAgeLessThanEqual(int age)` |
| `Like`, `Containing` | Pattern Matching | `LIKE '%...%'` | `findByNameContaining(String part)` |
| `NotLike`, `NotContaining` | Pattern Negation | `NOT LIKE '%...%'` | `findByNameNotContaining(String part)` |
| `StartingWith` | Prefix Matching | `LIKE '...%'` | `findByNameStartingWith(String prefix)` |
| `EndingWith` | Suffix Matching | `LIKE '%...'` | `findByNameEndingWith(String suffix)` |
| `IsNull` | Null Check | `IS NULL` | `findByEmailIsNull()` |
| `IsNotNull` | Non-null Check | `IS NOT NULL` | `findByEmailIsNotNull()` |
| `In` | Collection Inclusion | `IN (...)` | `findByStatusIn(Collection<String> statuses)` |
| `NotIn` | Collection Exclusion | `NOT IN (...)` | `findByStatusNotIn(Collection<String> statuses)` |
| `True` | Boolean True | `= true` | `findByActiveTrue()` |
| `False` | Boolean False | `= false` | `findByActiveFalse()` |

##### Combining Conditions

Conditions within method names can be combined using `And` and `Or`:

```java
List<User> findByNameAndActive(String name, boolean active);
List<User> findByNameOrEmail(String name, String email);
```

##### Limits and Sorting

Static limits (`First<N>`, `Top<N>`) and static ordering (`OrderBy<Property>Asc`, `OrderBy<Property>Desc`) are fully supported:

```java
List<User> findFirst10ByActiveTrueOrderByCreatedDateDesc();
Optional<User> findTopByStatusOrderByIdAsc(String status);
```

##### Projections and Counts

In addition to entity and collection returns, count and existence queries are supported:

```java
long countByNameStartingWith(String prefix);
boolean existsByEmail(String email);
```

##### Complete Repository Example

```java
@Repository
public interface PersonRepository extends LitebridgeRepository<Person, Long> {

    List<Person> findAllByNameAndSurname(String name, String surname);

    List<Person> findAllByNameOrSurname(String name, String surname);

    List<Person> findFirst10ByActiveTrueOrderByCreatedDateDesc();

    long countAllByNameStartingWith(String namePrefix);

    boolean existsByEmail(String email);

    Optional<Person> findByEmail(String email);
}
```

### Custom Repository Configuration

Explicit `@EnableLitebridgeRepositories` annotations take precedence over repository auto-configuration. When `@EnableLitebridgeRepositories` is declared, the auto-configuration backs off.

To completely disable repository auto-configuration:

```properties
litebridge.repositories.enabled=false
```
