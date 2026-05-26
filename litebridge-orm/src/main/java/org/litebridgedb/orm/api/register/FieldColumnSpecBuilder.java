package org.litebridgedb.orm.api.register;

import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.api.spec.FieldSpec;

import java.util.function.Function;

public final class FieldColumnSpecBuilder {

    public FieldColumnSpecBuilderFieldStep mapField(final String fieldName) {
        return new FieldColumnSpecBuilderFieldStep(new FieldSpec(fieldName, false));
    }

    public FieldColumnSpecBuilderFieldStep mapProperty(final String fieldName) {
        return new FieldColumnSpecBuilderFieldStep(new FieldSpec(fieldName, true));
    }

    public static FieldColumnSpec spec(Function<FieldColumnSpecBuilder, FieldColumnSpecBuilderTerminal> rc) {
        final FieldColumnSpecBuilderTerminal terminal = rc.apply(new FieldColumnSpecBuilder());

        return switch (terminal) {
            case FieldColumnSpecBuilderTerminalImpl t -> t.build();
            case FieldColumnSpecBuilderColumnStep t -> t.build();
            case FieldColumnSpecBuilderJoinStep t -> t.build();
        };
    }
}
