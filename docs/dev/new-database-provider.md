# Creating a new Database Provider

← [Litebridge Development](index.md)

Litebridge is designed to be extensible, allowing developers to add support for new database backends by implementing a database provider. 

This page details the steps required to implement a new database provider. 
The guidelines describe steps for database providers to be included with Litebridge, 
though apart from the naming and project structure the steps here can be followed to develop independent database providers as well.

## Implementation Steps

### 1. Create a New Module

New database providers should be added as a new module in the `litebridge-db` directory. Follow the naming convention `litebridge-db-<name>` (e.g., `litebridge-db-postgres`).

The `pom.xml` should use `litebridge-db` as its parent:

```xml
<parent>
    <groupId>org.litebridge.db</groupId>
    <artifactId>litebridge-db</artifactId>
    <version>${project.version}</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

Add dependencies on `litebridge-converter` (if you want to use the default type converter) and (optionally) `litebridge-db-spi-impl`:

```xml
<dependencies>
    <dependency>
        <!-- Litebridge default type converter -->
        <groupId>org.litebridge</groupId>
        <artifactId>litebridge-converter</artifactId>
    </dependency>
    <dependency>
        <!-- Provides the AbstractDatabaseProvider class -->
        <groupId>org.litebridge.db</groupId>
        <artifactId>litebridge-db-spi-impl</artifactId>
    </dependency>
    <!-- Your database JDBC driver -->
</dependencies>
```

### 2. Configure Modularity

Litebridge uses the Java Platform Module System (JPMS). You must include a `module-info.java` file in `src/main/java`. 
At should require `litebridge.db.spi` and (optionally) `litebridge.db.spi.impl` and `litebridge.converter`:

```java
@NullMarked
module litebridge.db.yourdb {
    requires org.jspecify;
    requires litebridge.converter;
    requires litebridge.db.spi;
    requires litebridge.db.spi.impl;
    requires org.slf4j;
    requires java.sql;

    exports org.litebridge.db.yourdb;
}
```

### 3. Implement the Provider

Implement the `DatabaseProvider` interface from the `litebridge.db.spi` module. However, it is highly recommended to extend `AbstractDatabaseProvider` from `litebridge.db.spi.impl` to benefit from its built-in SQL generation and execution logic.

#### Using AbstractDatabaseProvider

`AbstractDatabaseProvider` provides a skeletal implementation of the `DatabaseProvider` interface. It handles common tasks like:
- SQL statement preparation and execution.
- Metadata caching.
- Type conversion using a `TypeConverter`.
- Standard SQL generation for `SELECT`, `INSERT`, `UPDATE`, and `DELETE`.

By extending `AbstractDatabaseProvider`, you only need to focus on the database-specific SQL dialect and JDBC behaviors.

Key methods to override in your implementation:

- **Constructor**: Call `super(new DefaultTypeConverter())` or provide a custom converter.
- **`quoteIdentifier(String)`**: Define how to quote identifiers (tables, columns) to handle reserved words or case sensitivity.
- **`createAlias(String)`**: Define the syntax for aliases (e.g., whether to use `AS` for table aliases).
- **`appendLimitClause(Limit, StringBuilder)`**: Implement the specific pagination syntax for your database (e.g., `LIMIT/OFFSET`, `FETCH FIRST`, etc.).
- **`createSequenceNextValueForDirectInsert(String)`**: If your database supports sequences, provide the syntax for fetching the next value.
- **`createColumnIdentifier(...)`**: Customize how columns are referenced, especially in complex `JOIN` scenarios.
- **`getLogger()`**: Return a logger specific to your provider class.

Example snippet:

```java
public class YourDatabaseProvider extends AbstractDatabaseProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(YourDatabaseProvider.class);

    public YourDatabaseProvider() {
        super(new DefaultTypeConverter());
    }

    @Override
    protected String quoteIdentifier(String identifier) {
        // Example: double quote for case-sensitive or reserved words
        return "\"" + identifier + "\"";
    }

    @Override
    protected void appendLimitClause(Limit limit, StringBuilder sql) {
        limit.limit().ifPresent(l -> sql.append(" LIMIT ").append(l));
        limit.offset().ifPresent(o -> sql.append(" OFFSET ").append(o));
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
```

### 4. Testing

#### Unit Tests

Create unit tests in your module to verify that the generated SQL matches the expected dialect.

#### End-to-End Tests

To fully validate the provider, add it to the E2E test suite in `litebridge-orm`:
1. Add your new module as a test dependency in `litebridge-orm/pom.xml`.
2. Update the E2E test configuration to include your database (usually involving Testcontainers):
   1. Add a new `org.litebridge.orm.e2e.setup.DbEnvironment` implementation for your database.
   1. Update `org.litebridge.orm.e2e.setup.MultiDbTestExtension` and add your new environment as an invocation context.
4. Verify that all standard E2E tests pass against your new provider.

Refer to the [End-to-End Tests](tests.md) documentation for more details on running integration tests.
