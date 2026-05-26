package org.litebridgedb.orm.api.spec;

/**
 * Represents the mapping between a specific field in a Data Transfer Object (DTO) and its
 * corresponding database column specification.
 * <p>
 * This record combines a {@link FieldSpec} that defines the characteristics of the field
 * or property being mapped with a {@link ColumnMapping} that defines how the field maps
 * to the database representation. It is a core component of the ORM framework for defining
 * field-to-column mappings in a concise, type-safe manner.
 * <p>
 * The {@code FieldColumnSpec} record provides a utility method to retrieve the column
 * specification as a {@link ColumnSpec}, which is useful for direct column mappings.
 *
 * @param field  The {@link FieldSpec} representing the DTO field or property being mapped.
 * @param column The {@link ColumnMapping} representing the database mapping for the field.
 */
public record FieldColumnSpec(FieldSpec field, ColumnMapping column) {

    /**
     * Retrieves the database column specification as a {@link ColumnSpec}.
     * <p>
     * This method provides a type-safe way to access the column mapping for the associated
     * field in the form of a {@link ColumnSpec}, which represents the configuration and
     * metadata of the target database column.
     *
     * @return The {@link ColumnSpec} representing the column mapping for the field in the DTO.
     * @throws IllegalArgumentException if the provided column mapping is not an instance of {@link ColumnSpec}.
     */
    public ColumnSpec columnSpec() {
        if (column instanceof ColumnSpec columnSpec) {
            return columnSpec;
        } else {
            throw new IllegalArgumentException("Invalid column mapping provided: " + column);
        }
    }
}
