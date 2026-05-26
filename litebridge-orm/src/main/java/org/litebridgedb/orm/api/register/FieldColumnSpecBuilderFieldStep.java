package org.litebridgedb.orm.api.register;

import org.litebridgedb.orm.api.spec.FieldSpec;
import org.litebridgedb.orm.api.spec.ManyToMany;
import org.litebridgedb.orm.api.spec.OneToMany;

import java.util.function.Function;

public final class FieldColumnSpecBuilderFieldStep {

    private final FieldSpec fieldSpec;

    FieldColumnSpecBuilderFieldStep(final FieldSpec fieldSpec) {
        this.fieldSpec = fieldSpec;
    }

    public FieldColumnSpecBuilderColumnStep toColumn(final String column) {
        return new FieldColumnSpecBuilderColumnStep(fieldSpec, column);
    }

    public FieldColumnSpecBuilderTerminal oneToMany(Function<OneToManyBuilder, OneToMany> c) {
        return new FieldColumnSpecBuilderTerminalImpl(fieldSpec, c.apply(new OneToManyBuilder()));
    }

    public FieldColumnSpecBuilderTerminal manyToMany(Function<ManyToManyBuilder, ManyToMany> c) {
        return new FieldColumnSpecBuilderTerminalImpl(fieldSpec, c.apply(new ManyToManyBuilder()));
    }
}
