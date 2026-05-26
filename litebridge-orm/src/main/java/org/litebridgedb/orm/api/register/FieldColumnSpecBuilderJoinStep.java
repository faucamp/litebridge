package org.litebridgedb.orm.api.register;

import org.litebridgedb.orm.api.spec.ColumnSpec;
import org.litebridgedb.orm.api.spec.DtoTableSpec;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.api.spec.FieldSpec;
import org.litebridgedb.orm.api.spec.TableMapping;

import java.util.function.Function;

public final class FieldColumnSpecBuilderJoinStep implements FieldColumnSpecBuilderTerminal {

    private final FieldSpec fieldSpec;
    private final String column;
    private final String joinColumn;

    public FieldColumnSpecBuilderJoinStep(final FieldSpec fieldSpec,
                                          final String column,
                                          final String joinColumn) {
        this.fieldSpec = fieldSpec;
        this.column = column;
        this.joinColumn = joinColumn;
    }

    public FieldColumnSpecBuilderTerminal withMappedTable(final Class<?> dtoClass, final Function<RegistrationContext, RegistrationContextTerminal> rc) {
        final RegistrationContextTerminal context = rc.apply(new RegistrationContext(dtoClass, new PlaceHolderDatabaseProvider()));
        final DtoTableSpec dtoTableSpec = new DtoTableSpecBuilder(context).build();
        return new FieldColumnSpecBuilderTerminalImpl(fieldSpec, new ColumnSpec(column, null, joinColumn, new TableMapping(dtoClass, dtoTableSpec.tableSpec())));
    }

    FieldColumnSpec build() {
        return new FieldColumnSpec(fieldSpec, new ColumnSpec(column, null, joinColumn));
    }
}
