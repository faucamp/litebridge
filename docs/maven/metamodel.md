# Maven Goal: `metamodel`

← [Maven Plugin](index.md)

The `metamodel` goal scans your project for entities and DTOs and generates metamodel classes. 
These metamodels are used to provide type safety when building Litebridge queries.

## Configuration Parameters

| Parameter | Type | Required | Default | Description |
| :--- | :--- | :--- | :--- | :--- |
| `inputPackages` | `List<String>` | Yes | - | List of packages to scan for entity/DTO classes. |
| `outputPackage` | `String` | Yes | - | The package name for the generated metamodel classes. |
| `srcDir` | `String` | No | project source roots | Root directory where `inputPackages` will be searched for. Useful if generating metamodels from reverse-engineered entities. |
| `outputDir` | `String` | No | `${project.build.directory}/generated-sources/java` | The directory where generated metamodel files will be written. |
| `entitiesOnly` | `boolean` | No | `true` | If `true`, only classes annotated with `@Table` are processed. If `false`, all classes in the specified packages are processed. |
| `skip` | `boolean` | No | `false` | Skips goal execution if set to `true`. |

## Usage Example

```xml
<configuration>
    <inputPackages>
        <inputPackage>org.example.domain</inputPackage>
    </inputPackages>
    <outputPackage>org.example.meta</outputPackage>
    <!-- Optional: process both annotated entities and plain DTOs -->
    <entitiesOnly>false</entitiesOnly>
</configuration>
```

## Generated Metamodels

For each processed class (e.g., `User`), a corresponding metamodel class is generated (e.g., `UserMeta`). 
This class contains static `QueryField` constants for each field in the original class, which can be used 
in Litebridge's fluent API:

```java
// Using generated metamodel for a type-safe query with a SQL function
List<User> users = litebridge.select(User.class)
    .where(UserMeta.firstName.upper()).eq("MARY-JANE")
    .list();
```
