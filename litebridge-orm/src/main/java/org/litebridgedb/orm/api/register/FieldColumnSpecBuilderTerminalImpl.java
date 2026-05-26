package org.litebridgedb.orm.api.register;

import org.litebridgedb.orm.api.spec.ColumnMapping;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.api.spec.FieldSpec;

public final class FieldColumnSpecBuilderTerminalImpl implements FieldColumnSpecBuilderTerminal {

    private final FieldSpec fieldSpec;
    private final ColumnMapping columnMapping;

    public FieldColumnSpecBuilderTerminalImpl(final FieldSpec fieldSpec, final ColumnMapping columnMapping) {
        this.fieldSpec = fieldSpec;
        this.columnMapping = columnMapping;
    }

    public FieldColumnSpec build() {
        return new FieldColumnSpec(fieldSpec, columnMapping);
    }
}
