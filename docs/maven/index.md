# Litebridge Maven Plugin

← [Home](../index.md)

The Litebridge Maven Plugin provides tools to simplify working with Litebridge in a Maven-based project. 
It supports reverse-engineering database tables into Java entity classes and generating metamodels 
for type-safe querying.

## Goals

The plugin provides the following goals:

1.  [**`reverse-engineer`**](reverse-engineer.md): Connects to a database and generates Litebridge entity classes based on the database schema.
2.  [**`metamodel`**](metamodel.md): Scans a project for entities and/or DTOs and generates metamodel classes used for query type safety.

## Configuration

To use the Litebridge Maven Plugin, add it to the `<build>` section of the `pom.xml`:

```xml
<plugin>
    <groupId>org.litebridgedb.maven</groupId>
    <artifactId>litebridge-maven-plugin</artifactId>
    <version>0.3.0</version> <!-- Replace with latest version -->
    <executions>
        <execution>
            <goals>
                <goal>reverse-engineer</goal>
                <goal>metamodel</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Refer to the individual goal pages for detailed configuration options.
