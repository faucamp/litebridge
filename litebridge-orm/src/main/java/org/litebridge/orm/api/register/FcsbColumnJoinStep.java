package org.litebridge.orm.api.register;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.spec.ColumnMapping;
import org.litebridge.orm.api.spec.FieldColumnSpec;
import org.litebridge.orm.api.spec.FieldSpec;

import java.util.List;


public sealed class FcsbColumnJoinStep permits FieldColumnSpecBuilderColumnStep {

    final FieldSpec fieldSpec;
    final String column;
    private final @Nullable List<FieldColumnSpec> existingColumnMappings;

    FcsbColumnJoinStep(final FieldSpec fieldSpec, final String column) {
        this.fieldSpec = fieldSpec;
        this.column = column;
        this.existingColumnMappings = null;
    }

    FcsbColumnJoinStep(final FieldSpec fieldSpec, final String column, final List<FieldColumnSpec> existingColumnMappings) {
        this.fieldSpec = fieldSpec;
        this.column = column;
        this.existingColumnMappings = existingColumnMappings;
    }

    /**
     * Defines the join condition for a database column during field-to-column mapping configuration.
     *
     * @param column The name of the column from the joining table to be used in the join condition.
     *               Must not be null or empty.
     * @return An instance of {@link FieldColumnSpecBuilderJoinStep}, providing methods for further
     * configuration of the join or finalization of the column specification.
     */
    public FcsbJoinStepMultiColumn joinOn(final String column) {
        return new FcsbJoinStepMultiColumn(fieldSpec, this.column, column, existingColumnMappings);
    }

    /**
     * Configures the join condition for a database column using the previously set column
     * in the current field-to-column mapping configuration (i.e. a {@code JOIN USING} join).
     * <p>
     * This method utilizes the existing column specification and establishes a join
     * condition where the join is based on the given column. The resulting configuration
     * is represented as an instance of {@link FieldColumnSpecBuilderJoinStep}, providing
     * further options for refining the join or completing the mapping.
     *
     * @return An instance of {@link FieldColumnSpecBuilderJoinStep}, enabling additional
     * configuration of the database column join or finalization of the specification.
     */
    public FieldColumnSpecBuilderJoinStep joinUsing() {
        return joinOn(this.column);
    }
}
