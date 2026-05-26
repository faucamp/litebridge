package org.litebridgedb.orm.api.register;

import org.litebridgedb.db.spi.generator.ColumnValueGenerator;
import org.litebridgedb.orm.api.spec.ColumnSpec;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.api.spec.FieldSpec;

public final class FieldColumnSpecBuilderColumnStep implements FieldColumnSpecBuilderTerminal {

    final FieldSpec fieldSpec;
    final String column;

    FieldColumnSpecBuilderColumnStep(final FieldSpec fieldSpec, final String column) {
        this.fieldSpec = fieldSpec;
        this.column = column;
    }

    public FieldColumnSpecBuilderTerminal generateUsingSequence(final String sequence) {
        return generate(new PlaceholderSequenceColumnValueGenerator(sequence));
    }

    public FieldColumnSpecBuilderTerminal generate(ColumnValueGenerator generator) {
        return new FieldColumnSpecBuilderTerminalImpl(fieldSpec, new ColumnSpec(column, generator));
    }

    public FieldColumnSpecBuilderJoinStep joinOn(final String column) {
        return new FieldColumnSpecBuilderJoinStep(fieldSpec, this.column, column);
    }

    public FieldColumnSpecBuilderJoinStep joinUsing() {
        return joinOn(this.column);
    }

    FieldColumnSpec build() {
        return new FieldColumnSpec(fieldSpec, new ColumnSpec(column));
    }
}
