# Litebridge Type Converter

This module provides type converters for Litebridge. 

In the main `DefaultTypeConverter`, `Converter` implementations are loaded via a `ServiceLoader` 
and are provided by `module-info.java`.
In order to provide compatibility with older, non-JPMS frameworks such as Spring Boot 3, the converters are 
also provided by `META-INF/services` 

Documentation can be found [here](../docs/type-converter.md).