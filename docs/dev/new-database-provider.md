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
    <groupId>org.litebridgedb.db</groupId>
    <artifactId>litebridge-db</artifactId>
    <version>${project.version}</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

Add dependencies on `litebridge-converter` (if the default type converter is desired) and (optionally) `litebridge-db-spi-impl`:

```xml
<dependencies>
    <dependency>
        <!-- Litebridge default type converter -->
        <groupId>org.litebridgedb</groupId>
        <artifactId>litebridge-converter</artifactId>
    </dependency>
    <dependency>
        <!-- Provides the AbstractDatabaseProvider class -->
        <groupId>org.litebridgedb.db</groupId>
        <artifactId>litebridge-db-spi-impl</artifactId>
    </dependency>
    <!-- The database JDBC driver -->
</dependencies>
```

### 2. Configure Modularity

Litebridge uses the Java Platform Module System (JPMS). A `module-info.java` file must be included in `src/main/java`. 
It should require `litebridge.db.spi` and (optionally) `litebridge.db.spi.impl` and `litebridge.converter`, and must provide the `DatabaseProvider` implementation:

```java
@NullMarked
module litebridge.db.yourdb {
    requires org.jspecify;
    requires litebridge.converter;
    requires litebridge.db.spi;
    requires litebridge.db.spi.impl;
    requires org.slf4j;
    requires java.sql;

    provides org.litebridgedb.db.spi.DatabaseProvider with org.litebridgedb.db.yourdb.YourDatabaseProvider;

    exports org.litebridgedb.db.yourdb;
}
```

### 3. Implement the Provider

Implement the `DatabaseProvider` interface from the `litebridge.db.spi` module. However, it is highly recommended to extend `AbstractDatabaseProvider` from `litebridge.db.spi.impl` to benefit from its built-in SQL generation and execution logic.

#### Using AbstractDatabaseProvider

`AbstractDatabaseProvider` provides a skeletal implementation of the `DatabaseProvider` interface. It handles common tasks like:
- SQL statement preparation and execution.
- Metadata caching.
- Type conversion using a `TypeConverter`.
- Delegating SQL generation for `SELECT`, `INSERT`, `UPDATE`, and `DELETE` to specialized generator classes.

By extending `AbstractDatabaseProvider`, the focus can be placed on the database-specific SQL dialect and JDBC behaviors by providing custom implementations of the various SQL generators and transformers.

Key methods to override in the provider implementation:

- **Constructor**: Call `super(new DefaultTypeConverter())` or provide a custom converter.
- **`createColumnIdentifierGenerator()`**: Return a custom `ColumnIdentifierGenerator` to define how to quote identifiers (tables, columns) and handle alias declarations.
- **`createSelectSqlGenerator()`**: Return a custom `SelectSqlGenerator` to implement specific pagination syntax or other `SELECT` statement customizations.
- **`createAliasTransformer()`**: Return a custom `AliasTransformer` if the database expects specific alias formatting (e.g., all uppercase).
- **`createPreparedStatementUsingConnection(...)`**: Customize how `PreparedStatement` instances are created, for example to handle generated keys differently.
- **`extractGeneratedKeys(...)`**: Customize how generated keys are retrieved from the `PreparedStatement` after an insert.
- **`getSequenceColumnValueGenerator(String)`**: If the database supports sequences, provide an implementation that returns the SQL for fetching the next value.
- **`getLogger()`**: Return a logger specific to the provider class.

#### Customizing SQL Generation

To customize the SQL dialect, subclasses of `ColumnIdentifierGenerator` or `SelectSqlGenerator` are typically created.

##### Customizing Identifiers

Extend `ColumnIdentifierGenerator` to override methods like:
- `quoteIdentifier(String)`: Define how to quote identifiers.
- `createAliasDeclaration(String)`: Define the syntax for aliases.

##### Customizing SELECT Statements

Extend `SelectSqlGenerator` to override methods like:
- `appendLimitClause(Limit, StringBuilder)`: Implement the specific pagination syntax for the database (e.g., `LIMIT/OFFSET`, `FETCH FIRST`, etc.).

Example snippet:

```java
public class YourDatabaseProvider extends AbstractDatabaseProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(YourDatabaseProvider.class);

    public YourDatabaseProvider() {
        super(new DefaultTypeConverter());
    }

    @Override
    protected ColumnIdentifierGenerator createColumnIdentifierGenerator() {
        return new YourColumnIdentifierGenerator();
    }

    @Override
    protected SelectSqlGenerator createSelectSqlGenerator() {
        return new YourSelectSqlGenerator(typeConverter, columnIdentifierGenerator.orThrow(), this::ensureTableMetaData);
    }

    @Override
    public SequenceColumnValueGenerator getSequenceColumnValueGenerator(String sequence) {
        return new YourSequenceColumnValueGenerator(sequence);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}

class YourColumnIdentifierGenerator extends ColumnIdentifierGenerator {
    @Override
    public String quoteIdentifier(String identifier) {
        // Example: double quote for case-sensitive or reserved words
        return "\"" + identifier + "\"";
    }
}

class YourSelectSqlGenerator extends SelectSqlGenerator {
    public YourSelectSqlGenerator(TypeConverter typeConverter, 
                                  ColumnIdentifierGenerator columnIdentifierGenerator, 
                                  BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData) {
        super(typeConverter, columnIdentifierGenerator, ensureTableMetaData);
    }

    @Override
    protected void appendLimitClause(Limit limit, StringBuilder sql) {
        limit.limit().ifPresent(l -> sql.append(" LIMIT ").append(l));
        limit.offset().ifPresent(o -> sql.append(" OFFSET ").append(o));
    }
}

class YourSequenceColumnValueGenerator extends SequenceColumnValueGenerator {
    public YourSequenceColumnValueGenerator(String sequence) {
        super(sequence);
    }

    @Override
    public String generate(ColumnMetaData columnMetaData) {
        return "NEXT VALUE FOR " + sequence;
    }
}
```

### 4. Testing

#### Unit Tests

Create unit tests in the module to verify that the generated SQL matches the expected dialect.

#### End-to-End Tests

To fully validate the provider, it can be added to the E2E test suite in `litebridge-orm`:
1. Add the new module as a test dependency in `litebridge-orm/pom.xml`.
2. Update the E2E test configuration to include the database (usually involving Testcontainers):
   1. Add a new `org.litebridgedb.orm.e2e.setup.DbEnvironment` implementation for the database.
   1. Update `org.litebridgedb.orm.e2e.setup.MultiDbTestExtension` and add the new environment as an invocation context.
4. Verify that all standard E2E tests pass against the new provider.

Refer to the [End-to-End Tests](tests.md) documentation for more details on running integration tests.
