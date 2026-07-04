# Maven Goal: `metamodel`

← [Maven Plugin](index.md)

The `metamodel` goal scans your project for entities and DTOs and generates metamodel classes. 
These metamodels are used to provide type safety when building Litebridge queries.

## Configuration Parameters

| Parameter | Type | Required | Default | Description |
| :--- | :--- | :--- | :--- | :--- |
| `input` | `MetamodelInputConfig` | Yes | - | Input configuration (packages to scan, source directory). |
| `output` | `MetamodelOutputConfig` | Yes | - | Output configuration (package name, directory, class names). |
| `skip` | `boolean` | No | `false` | Skips goal execution if set to `true`. |

### `input` Settings

| Parameter | Type | Required | Default | Description |
| :--- | :--- | :--- | :--- | :--- |
| `inputPackages` | `List<String>` | Yes | - | List of packages to scan for entity/DTO classes. |
| `srcDir` | `String` | No | project source roots | Root directory where `inputPackages` will be searched for. Useful if generating metamodels from reverse-engineered entities. |
| `entitiesOnly` | `boolean` | No | `true` | If `true`, only classes annotated with `@Table` are processed. If `false`, all classes in the specified packages are processed. |

### `output` Settings

| Parameter | Type | Required | Default | Description |
| :--- | :--- | :--- | :--- | :--- |
| `outputPackage` | `String` | Yes | - | The package name for the generated metamodel classes. |
| `outputDir` | `String` | No | `${project.build.directory}/generated-sources/java` | The directory where generated metamodel files will be written. |
| `classNamePrefix` | `String` | No | (empty) | Prefix to add to generated class names. |
| `classNameSuffix` | `String` | No | `Meta`* | Suffix to add to generated class names. |
| `packageInfo` | `boolean` | No | `true` | Whether to generate a `package-info.java` file. |
| `javadoc` | `boolean` | No | `true` | Whether to include Javadoc comments in generated classes. |
| `finalClasses` | `boolean` | No | `true` | Whether to declare generated metamodel classes as `final`. |

\* If `classNamePrefix` is specified and `classNameSuffix` is not, `classNameSuffix` defaults to an empty string. If neither is specified, `classNameSuffix` defaults to `Meta`.

## Usage Example

```xml
<configuration>
    <input>
        <inputPackages>
            <inputPackage>org.example.domain</inputPackage>
        </inputPackages>
        <!-- Optional: process both annotated entities and plain DTOs -->
        <entitiesOnly>false</entitiesOnly>
    </input>
    <output>
        <outputPackage>org.example.meta</outputPackage>
        <!-- Optional: custom name for generated metamodels (e.g. UserModel) -->
        <classNameSuffix>Model</classNameSuffix>
    </output>
</configuration>
```

## Generated Metamodels

The default suffix for generated metamodels is `Meta`.

For each processed class (e.g., `User`), a corresponding metamodel class is generated (e.g., `UserMeta`). 
This class contains static `QueryField` constants for each field in the original class, which can be used 
in Litebridge's fluent API:

```java
// Using generated metamodel for a type-safe query with a SQL function
List<User> users = litebridge.select(User.class)
    .where(UserMeta.firstName.upper()).eq("MARY-JANE")
    .list();
```
