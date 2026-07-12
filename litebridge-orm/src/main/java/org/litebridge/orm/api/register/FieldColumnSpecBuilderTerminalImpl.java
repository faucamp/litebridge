package org.litebridge.orm.api.register;

import org.litebridge.orm.api.spec.ColumnMapping;
import org.litebridge.orm.api.spec.FieldColumnSpec;
import org.litebridge.orm.api.spec.FieldSpec;

/**
 * Final implementation of the {@code FieldColumnSpecBuilderTerminal} interface, used to build
 * a {@link FieldColumnSpec} instance representing the mapping between a DTO field and its
 * corresponding database column or relationship specification.
 *
 * <p>This class serves as the terminal step in a fluent API for defining field-to-column
 * mappings within the ORM framework. It combines a {@link FieldSpec}, representing the DTO field,
 * with a {@link ColumnMapping}, detailing the mapping relationship or column specification.
 *
 * <p>The {@code build} method finalizes the configuration and returns a {@link FieldColumnSpec}
 * instance, which encapsulates the field and its corresponding column mapping. Instances of this
 * class are immutable once created.
 * <p>
 * Constructor:
 * - Requires a {@link FieldSpec} defining the DTO field to be mapped.
 * - Requires a {@link ColumnMapping} specifying how the field maps to the database.
 * <p>
 * Implements:
 * - {@link FieldColumnSpecBuilderTerminal}, as the final step in the builder pattern for field-to-column mappings.
 * <p>
 * Thread Safety:
 * - Immutable and thread-safe, as all fields are final and this class performs no mutable operations.
 * <p>
 * Usage:
 * - This class should only be used as part of the ORM builder framework and not instantiated directly.
 */
public final class FieldColumnSpecBuilderTerminalImpl implements FieldColumnSpecBuilderTerminal {

    private final FieldSpec fieldSpec;
    private final ColumnMapping columnMapping;

    /**
     * Constructs a new {@code FieldColumnSpecBuilderTerminalImpl}.
     *
     * @param fieldSpec     the field specification.
     * @param columnMapping the column mapping.
     */
    public FieldColumnSpecBuilderTerminalImpl(final FieldSpec fieldSpec, final ColumnMapping columnMapping) {
        this.fieldSpec = fieldSpec;
        this.columnMapping = columnMapping;
    }

    /**
     * Builds the field column specification.
     *
     * @return the built field column specification.
     */
    public FieldColumnSpec build() {
        return new FieldColumnSpec(fieldSpec, columnMapping);
    }
}
