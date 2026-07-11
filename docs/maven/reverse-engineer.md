# Maven Goal: `reverse-engineer`

← [Maven Plugin](index.md)

The `reverse-engineer` goal connects to a database and generates Litebridge entity classes based on the database schema.

## Configuration Parameters

| Parameter         | Type                         | Required | Default | Description                                     |
|:------------------|:-----------------------------|:---------|:--------|:------------------------------------------------|
| `database`        | `DatabaseConfig`             | Yes      | -       | Database connection settings.                   |
| `input`           | `RevEngInputConfig`          | Yes      | -       | Specifies which tables to reverse engineer.     |
| `output`          | `RevEngOutputConfig`         | Yes      | -       | Settings for the generated source files.        |
| `sqlTypeMappings` | `List<SqlTypeMappingConfig>` | No       | -       | Custom mapping of SQL types to Java types.      |
| `tableMappings`   | `List<TableMappingConfig>`   | No       | -       | Custom mapping for specific tables and columns. |
| `skip`            | `boolean`                    | No       | `false` | Skips goal execution if set to `true`.          |

### `database` Settings

| Parameter               | Type     | Required | Default | Description                                                                                                                     |
|:------------------------|:---------|:---------|:--------|:--------------------------------------------------------------------------------------------------------------------------------|
| `databaseProviderClass` | `String` | Yes      | -       | The fully qualified class name of the Litebridge `DatabaseProvider` to use (e.g., `org.litebridgedb.db.h2.H2DatabaseProvider`). |
| `url`                   | `String` | Yes      | -       | JDBC connection URL.                                                                                                            |
| `user`                  | `String` | Yes      | -       | Database username.                                                                                                              |
| `password`              | `String` | No       | (empty) | Database password.                                                                                                              |

### `input` Settings

| Parameter | Type           | Required | Default | Description                                                                                       |
|:----------|:---------------|:---------|:--------|:--------------------------------------------------------------------------------------------------|
| `tables`  | `List<String>` | Yes      | -       | List of table names to generate entities for. Names should be qualified with schema if necessary. |

### `output` Settings

| Parameter       | Type                   | Required | Default                                             | Description                                               |
|:----------------|:-----------------------|:---------|:----------------------------------------------------|:----------------------------------------------------------|
| `outputPackage` | `String`               | Yes      | -                                                   | The package name for the generated entity classes.        |
| `outputDir`     | `String`               | No       | `${project.build.directory}/generated-sources/java` | The directory where generated Java files will be written. |
| `packageInfo`   | `boolean`              | No       | `true`                                              | Whether to generate a `package-info.java` file.           |
| `javadoc`       | `boolean`              | No       | `true`                                              | Whether to include Javadoc comments in generated classes. |
| `finalClasses`  | `boolean`              | No       | `true`                                              | Whether to declare generated entity classes as `final`.   |
| `jspecify`      | `RevEngJSpecifyConfig` | No       | -                                                   | Configuration for JSpecify nullability annotations.       |

#### `jspecify` Settings

| Parameter          | Type      | Required | Default | Description                                                                                                                                                                                                              |
|:-------------------|:----------|:---------|:--------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `annotate`         | `boolean` | No       | `false` | Enable JSpecify annotations in generated code.                                                                                                                                                                           |
| `nullMarked`       | `boolean` | No       | `true`  | Use `@NullMarked` at the class or package level. If `false`, `@NullUnmarked` is used. This setting is only used if `annotate` is `true`.                                                                                 |
| `databaseNullable` | `boolean` | No       | `false` | If `true`, fields are marked `@Nullable` only if the database column is nullable. If `false`, all non-primitive fields are marked `@Nullable`. This setting is only used if `annotate` and `nullMarked` are both `true`. |

### `sqlTypeMappings` Settings

Allows overriding the default Java type mapping for specific SQL types.

| Parameter   | Type      | Required | Default | Description                                    |
|:------------|:----------|:---------|:--------|:-----------------------------------------------|
| `jdbcType`  | `String`  | Yes      | -       | JDBC type name (e.g., `NUMERIC`, `VARCHAR`).   |
| `fieldType` | `String`  | Yes      | -       | Fully qualified Java class name for the field. |
| `precision` | `Integer` | No       | -       | Optional precision to match.                   |
| `notNull`   | `Boolean` | No       | -       | Optional nullability to match.                 |

### `tableMappings` Settings

Allows fine-grained control over how specific tables and columns are mapped.

| Parameter        | Type                        | Required | Default | Description                                      |
|:-----------------|:----------------------------|:---------|:--------|:-------------------------------------------------|
| `table`          | `String`                    | Yes      | -       | The database table name this mapping applies to. |
| `entityName`     | `String`                    | No       | -       | Explicit name for the generated entity class.    |
| `columnMappings` | `List<ColumnMappingConfig>` | No       | -       | Custom mappings for specific columns.            |

#### `columnMappings` Settings

| Parameter               | Type     | Required | Default | Description                                              |
|:------------------------|:---------|:---------|:--------|:---------------------------------------------------------|
| `column`                | `String` | Yes      | -       | The database column name.                                |
| `fieldName`             | `String` | No       | -       | Explicit name for the generated Java field.              |
| `fieldType`             | `String` | No       | -       | Explicit Java type for the field.                        |
| `generateUsingSequence` | `String` | No       | -       | Name of a database sequence to use for value generation. |
| `generatorClass`        | `String` | No       | -       | Fully qualified class name of a custom value generator.  |

## Usage Example

```xml
<plugin>
    <groupId>org.litebridgedb.maven</groupId>
    <artifactId>litebridge-maven-plugin</artifactId>
    <version>0.3.0</version> <!-- Replace with latest version -->
    <executions>
        <execution>
            <goals>
                <goal>reverse-engineer</goal>
            </goals>
            <configuration>
                <!-- Specify database connection -->
                <database>
                    <url>jdbc:h2:mem:test</url>
                    <user>sa</user>
                    <databaseProviderClass>org.litebridgedb.db.h2.H2DatabaseProvider</databaseProviderClass>
                </database>

                <!-- Specify input tables to reverse engineer -->
                <input>
                    <tables>
                        <!-- Format: <schema>.<table> -->
                        <table>PUBLIC.USERS</table>
                        <table>PUBLIC.POSTS</table>
                    </tables>
                </input>

                <!-- Optional: set global default type mappings -->
                <sqlTypeMappings>
                    <!-- Ensures Long is used for fields mapping to the JDBC type NUMERIC -->
                    <sqlTypeMapping>
                        <jdbcType>NUMERIC</jdbcType>
                        <fieldType>java.lang.Long</fieldType>
                    </sqlTypeMapping>
                    <!-- Map NOT NULL NUMERICs of size 1 to primitive booleans -->
                    <sqlTypeMapping>
                        <jdbcType>NUMERIC</jdbcType>
                        <precision>1</precision>
                        <notNull>true</notNull>
                        <fieldType>boolean</fieldType>
                    </sqlTypeMapping>
                    <!-- Map nullable NUMERICs of size 1 to Boolean objects -->
                    <sqlTypeMapping>
                        <jdbcType>NUMERIC</jdbcType>
                        <precision>1</precision>
                        <notNull>false</notNull>
                        <fieldType>java.lang.Boolean</fieldType>
                    </sqlTypeMapping>
                </sqlTypeMappings>

                <!-- Optional: specify table-specific customisations -->
                <tableMappings>
                    <tableMapping>
                        <table>PUBLIC.USERS</table>
                        <columnMappings>
                            <columnMapping>
                                <column>USER_ID</column>
                                <!-- Overrides the field name from "userId" to "id" -->
                                <fieldName>id</fieldName>
                                <!-- Specifies that the value of this column is generated using a sequence -->
                                <generateUsingSequence>PUBLIC.USER_SEQ</generateUsingSequence>
                            </columnMapping>
                        </columnMappings>
                    </tableMapping>
                </tableMappings>

                <!-- Specify output location, package and entity generation customisation -->
                <output>
                    <outputPackage>org.example.domain</outputPackage>
                    <jspecify>
                        <!-- Adds JSpecify nullability annotations to generated entities -->
                        <annotate>true</annotate>
                    </jspecify>
                </output>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Relationship Handling

The `reverse-engineer` goal automatically detects relationships between tables based on foreign key constraints in the
database.

### Many-to-One Relationships

When a table has a foreign key referencing another table that is also being reverse-engineered, the plugin generates a
field with the related entity's type instead of a scalar type (like `Long`).

* **Detection:** Based on foreign key constraints.
* **Mapping:** Uses the `@Column` annotation with the `joinOn` attribute specifying the join column in the related
  entity.

### One-to-Many Relationships

For every Many-to-One relationship detected, Litebridge can generate a corresponding reverse One-to-Many relationship in
the referenced entity.

* **Detection:** Automatically created for the "other side" of a foreign key relationship.
* **Mapping:** Uses a `List<RelatedEntity>` field annotated with `@OneToMany(mappedByField = "...")`.

### Many-to-Many Relationships

Many-to-Many relationships are detected when a join table exists that is not explicitly included in the `tables`
configuration but contains exactly two foreign keys referencing two tables that *are* included.

* **Detection:** Join tables with two foreign keys to mapped entities.
* **Mapping:** Generates `List<RelatedEntity>` fields in both related entities, annotated with `@ManyToMany`.

## Generated Entity Examples

Below are snippets of generated entity classes showing various configuration options and relationship mappings.

### Basic Column Mapping and Sequences

Based on the `tableMappings` and `sqlTypeMappings` in the configuration example:

```java
@Table("PUBLIC.USERS")
public final class User {

    @Column(value = "USER_ID", generateUsingSequence = "PUBLIC.USER_SEQ")
    private Long id;

    @Column("USERNAME")
    private String username;

    // ... getters and setters ...
}
```

### Many-to-One and One-to-Many

Example of an `Account` entity belonging to a `Person`:

**Account.java (Many-to-One side)**

```java
@Table("LB.ACCOUNT")
public final class Account {

    @Column(value = "PERSON_ID", joinOn = "id")
    private PersonEntity owner;

    // ...
}
```

**PersonEntity.java (One-to-Many side)**

```java
@Table("LB.PERSON")
public final class PersonEntity {

    @Column(value = "PERSON_ID")
    private Long id;

    @OneToMany(mappedByField = "owner")
    private List<Account> accounts;

    // ...
}
```

### Many-to-Many

Example of a relationship between `PersonEntity` and `Address` via a `PERSON_ADDRESS` join table:

```java
@Table("LB.PERSON")
public final class PersonEntity {

    @ManyToMany(joinTable = "LB.PERSON_ADDRESS", joinColumn = "PERSON_ID", inverseJoinColumn = "ADDRESS_ID")
    private List<Address> addresses;

    // ...
}
```

```java
@Table("LB.ADDRESS")
public final class Address {

    @ManyToMany(joinTable = "LB.PERSON_ADDRESS", joinColumn = "ADDRESS_ID", inverseJoinColumn = "PERSON_ID")
    private List<PersonEntity> personEntities;

    // ...
}
```

### JSpecify Nullability

When JSpecify support is enabled (`<annotate>true</annotate>`), generated entities include `@Nullable` and `@NullMarked`
annotations.

```java
@NullMarked
@Table("LB.PERSON")
public final class PersonEntity {

    @Nullable
    @Column("SURNAME")
    private String surname;

    @Column("AGE")
    private int age; // Primitive because it's NOT NULL in DB or mapped specifically

    // ...
}
```
