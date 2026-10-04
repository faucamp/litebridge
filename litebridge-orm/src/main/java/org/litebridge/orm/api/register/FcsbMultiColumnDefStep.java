package org.litebridge.orm.api.register;

import org.litebridge.orm.api.spec.FieldColumnSpec;
import org.litebridge.orm.api.spec.FieldSpec;

import java.util.List;

/**
 * Configures a single column mapping in a multi-column field-to-column mapping step.
 */
public final class FcsbMultiColumnDefStep {

    private final FieldSpec fieldSpec;
    private final List<FieldColumnSpec> columnMappings;

    FcsbMultiColumnDefStep(final FieldSpec fieldSpec, final List<FieldColumnSpec> columnMappings) {
        this.fieldSpec = fieldSpec;
        this.columnMappings = columnMappings;
    }

    /**
     * Starts the mapping of a single column for the current field for a multi-column field mapping.
     * <p>
     * This method is used to define the name of a single column associated with the field
     * being configured, in a journey of configuring a multi-column field-to-column mapping.
     *
     * @param column The name of one of the database columns to which the field should be mapped.
     *               Must not be null or empty.
     * @return Next step in the chain to define the join parameters for this column.
     */
    public FcsbColumnJoinStep column(final String column) {
        return new FcsbColumnJoinStep(fieldSpec, column, columnMappings);
    }
}
