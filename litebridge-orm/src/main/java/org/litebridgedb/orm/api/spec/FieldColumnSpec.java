package org.litebridgedb.orm.api.spec;

/**
 * Represents the mapping between a specific field in a Data Transfer Object (DTO) and its
 * corresponding database column specification.
 * <p>
 * This record combines a {@link FieldSpec} that defines the characteristics of the field
 * or property being mapped with a {@link ColumnMapping} that defines how the field maps
 * to the database representation. It is a core component of the ORM framework for defining
 * field-to-column mappings in a concise, type-safe manner.
 *
 * @param field  The {@link FieldSpec} representing the DTO field or property being mapped.
 * @param column The {@link ColumnMapping} representing the database mapping for the field.
 */
public record FieldColumnSpec(FieldSpec field, ColumnMapping column) {
}
