package org.litebridge.orm.api.register;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.spec.ColumnSpec;
import org.litebridge.orm.api.spec.FieldColumnSpec;
import org.litebridge.orm.api.spec.FieldSpec;
import org.litebridge.orm.api.spec.MultiColumnSpec;

import java.util.ArrayList;
import java.util.List;

public final class FcsbJoinStepMultiColumn extends FieldColumnSpecBuilderJoinStep {

    /**
     * Creates a new {@code FcsbJoinStepMultiColumn} instance.
     *
     * @param fieldSpec              Field specification being configured.
     * @param column                 Current column name.
     * @param joinColumn             Join column name.
     * @param existingColumnMappings Other columns mapped to the same field.
     */
    public FcsbJoinStepMultiColumn(final FieldSpec fieldSpec,
                                   final String column,
                                   final String joinColumn,
                                   final @Nullable List<FieldColumnSpec> existingColumnMappings) {
        super(fieldSpec, column, joinColumn, existingColumnMappings);
    }

    /**
     * Adds another column to the multi-column field mapping.
     *
     * @param column The name of the database column to which the field should be mapped.
     *               Must not be null or empty.
     * @return Next step in the chain to define the join parameters for this column.
     */
    public FcsbColumnJoinStep column(final String column) {
        // Store the current field column spec and start adding another one
        final List<FieldColumnSpec> existingColumnMappings = this.existingColumnMappings == null ? new ArrayList<>() : this.existingColumnMappings;
        existingColumnMappings.add(super.build());
        return new FieldColumnSpecBuilderColumnStep(fieldSpec, column, existingColumnMappings);
    }

    @Override
    FieldColumnSpec build() {
        // Create the current/last field column spec
        final FieldColumnSpec fieldColumnSpec = super.build();

        // Add it to the existing ones, or return it directly if no other columns were present
        if (existingColumnMappings == null) {
            return fieldColumnSpec;
        }

        existingColumnMappings.add(fieldColumnSpec);

        final ColumnSpec[] columnSpecs = new ColumnSpec[existingColumnMappings.size()];

        for (int i = 0; i < columnSpecs.length; i++) {
            columnSpecs[i] = (ColumnSpec) existingColumnMappings.get(i).column();
        }

        return new FieldColumnSpec(fieldSpec, new MultiColumnSpec(columnSpecs));
    }
}
