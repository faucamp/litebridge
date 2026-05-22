package org.litebridgedb.orm.api.spec;

import java.lang.invoke.MethodHandles;

/**
 * Represents the mapping between a Data Transfer Object (DTO) class and a database table,
 * along with a method lookup used for runtime operations.
 * <p>
 * This record encapsulates the following:
 * - A {@link MethodHandles.Lookup} instance, providing a lookup mechanism for accessing methods or fields dynamically.
 * - The DTO class that defines the structure and data mappings corresponding to a database table.
 * - A {@link TableSpec} instance containing the specification for the database table to map the DTO.
 * <p>
 * The {@code TableMapping} is used as part of an ORM (Object-Relational Mapping) system
 * to relate Java objects to database tables and their respective fields/columns.
 * <p>
 * Note:
 * - The primary constructor allows specifying all three components explicitly.
 * - An overloaded constructor provides a default lookup mechanism using {@code MethodHandles.publicLookup()}.
 *
 * @param lookup    The {@link MethodHandles.Lookup} instance for performing reflective operations on the DTO class.
 * @param dtoClass  The DTO class that represents the mapping target.
 * @param tableSpec The {@link TableSpec} defining the corresponding database table and its field-column mappings.
 */
public record TableMapping(MethodHandles.Lookup lookup, Class<?> dtoClass, TableSpec tableSpec) {

    public TableMapping(Class<?> dtoClass, TableSpec tableSpec) {
        this(MethodHandles.publicLookup(), dtoClass, tableSpec);
    }
}
