# Type Converter

← [Home](index.md)

The `litebridge-converter` module provides a flexible and extensible type conversion system. It is used by the Litebridge ORM to translate between Java types and SQL-specific types, but it can also be used as a standalone library for general-purpose type conversion.

## Core Interfaces

The type conversion system is built around several key interfaces:

### `TypeConverter`

Located in the `litebridge-db-spi` module, `TypeConverter` is the primary entry point for type conversion logic. It defines two main conversion methods:

- `Object convert(Object value, int dbDataType)`: Converts a value to a database-specific representation (or vice-versa) based on the `java.sql.Types` code.
- `<T> T convert(Object value, Class<T> targetType)`: Converts a value to a specific Java type.

### `Converter<T>`

Extends `ConverterFunction<T>` and represents a converter for a specific Java type. It provides:
- `Class<?> type()`: The target Java class this converter handles.
- `Class<?> primitiveType()`: (Optional) The primitive counterpart of the target class.

### `SqlConverter<T>`

Extends `Converter<T>` and adds SQL-specific metadata:
- `int[] sqlTypes()`: An array of `java.sql.Types` integer codes that this converter is associated with.

### `ConverterFunction<T>`

A functional interface with a single method `@Nullable T convert(@Nullable Object value)`. This allows using lambda expressions for simple conversion logic.

## Implementations

### `ConfigurableTypeConverter`

`ConfigurableTypeConverter` is a concrete implementation of `TypeConverter` that allows manual registration and unregistration of converters.

- `register(Converter<?> converter)`: Registers a new converter.
- `register(Class<T> type, int[] sqlTypes, ConverterFunction<T> function)`: A convenient way to register a converter using a functional interface.
- `unregister(Class<?> type)`: Removes a converter for a specific Java type.
- `unregister(int sqlType)`: Removes a converter for a specific SQL type.

### `DefaultTypeConverter`

`DefaultTypeConverter` extends `ConfigurableTypeConverter` and automatically populates itself with all `Converter` implementations found using the Java `ServiceLoader` mechanism. 

#### Dynamic Loading via JPMS

Litebridge leverages the Java Platform Module System (JPMS) to allow for dynamic and extensible type conversion. The `litebridge-converter` module defines the `Converter` and `SqlConverter` service interfaces. 

The `DefaultTypeConverter` uses `ServiceLoader.load(Converter.class)` to discover and register all available converter implementations at runtime. This allows:
- **Modular Extensibility**: Custom modules can provide their own `Converter` implementations by including a `provides` clause in their `module-info.java`.
- **Automatic Registration**: Any converter service found on the module path or classpath is automatically picked up and registered by the `DefaultTypeConverter` without any manual configuration.
- **Dynamic Overrides**: By providing a custom converter with higher precedence or by registering it manually in a `ConfigurableTypeConverter`, the conversion logic for any type can be easily customized.

For example, a module providing a custom converter for a specific library type would include:

```java
module my.custom.module {
    requires litebridge.converter;
    provides org.litebridge.convert.converter.Converter with my.package.MyCustomConverter;
}
```

## Standalone Usage

The `litebridge-converter` module is designed to be independent. The module can be used in any project by adding the dependency and instantiating a `TypeConverter`:

```java
// Using the default converters
TypeConverter converter = new DefaultTypeConverter();

// Basic conversion
Integer intValue = converter.convert("123", Integer.class);
String stringValue = converter.convert(456, String.class);

// SQL-type based conversion
Boolean boolValue = (Boolean) converter.convert(1, java.sql.Types.BOOLEAN);
```

The `ConfigurableTypeConverter` (and `DefaultTypeConverter`) can be used to define custom converters in-line:

```java
ConfigurableTypeConverter customConverter = new ConfigurableTypeConverter();

// Register a custom converter using a converter function 
customConverter.register(MyCustomType.class, value -> {
    // Custom logic here
    return new MyCustomType(value.toString());
});

// SQL types can also be specified this way
customConverter.register(MyCustomType.class, new int[]{java.sql.Types.OTHER}, value -> {
    // Custom logic here
    return new MyCustomType(value.toString());
});
```

Additionally, existing converters (e.g. for SQL types) can be replaced:

```java
DefaultTypeConverter defaultConverter = new DefaultTypeConverter();

// Override to convert NUMERIC SQL types to Long (instead of the default BigDecimal)
defaultConverter.register(Long.class, new int[]{java.sql.Types.NUMERIC}, new LongConverter());
```

## Built-in Converters

Litebridge comes with a wide range of built-in converters for common Java and SQL types:

- **Primitive & Wrapper Types**: `Boolean`, `Byte`, `Character`, `Short`, `Integer`, `Long`, `Float`, `Double`.
- **Numeric Types**: `BigDecimal`, `BigInteger`.
- **String Types**: `String`.
- **Date & Time Types**: `java.util.Date`, `java.time.LocalDate`, `java.time.LocalDateTime`, `java.time.OffsetDateTime`, `java.time.ZonedDateTime`.
- **SQL Date/Time Types**: `java.sql.Date`, `java.sql.Time`, `java.sql.Timestamp`.
- **Other**: `byte[]` (Binary data).

Most of these converters handle common conversions, such as parsing strings to numbers or converting between different numeric types.
