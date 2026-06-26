package org.litebridgedb.orm.api.spec;

/**
 * Represents the mapping between a specific field in a Data Transfer Object (DTO) and its
 * corresponding database lhs specification.
 * <p>
 * This record combines a {@link FieldSpec} that defines the characteristics of the field
 * or property being mapped with a {@link ColumnMapping} that defines how the field maps
 * to the database representation. It is a core component of the ORM framework for defining
 * field-to-lhs mappings in a concise, type-safe manner.
 * <p>
 * The {@code FieldColumnSpec} record provides a utility method to retrieve the lhs
 * specification as a {@link ColumnSpec}, which is useful for direct lhs mappings.
 *
 * @param field  The {@link FieldSpec} representing the DTO field or property being mapped.
 * @param column The {@link ColumnMapping} representing the database mapping for the field.
 */
public record FieldColumnSpec(FieldSpec field, ColumnMapping column) {

    /**
     * Retrieves the database lhs specification as a {@link ColumnSpec}.
     * <p>
     * This method provides a type-safe way to access the lhs mapping for the associated
     * field in the form of a {@link ColumnSpec}, which represents the configuration and
     * metadata of the target database lhs.
     *
     * @return The {@link ColumnSpec} representing the lhs mapping for the field in the DTO.
     * @throws IllegalArgumentException if the provided lhs mapping is not an instance of {@link ColumnSpec}.
     */
    public ColumnSpec columnSpec() {
        if (column instanceof ColumnSpec columnSpec) {
            return columnSpec;
        } else {
            throw new IllegalArgumentException("Invalid lhs mapping provided: " + column);
        }
    }
}
