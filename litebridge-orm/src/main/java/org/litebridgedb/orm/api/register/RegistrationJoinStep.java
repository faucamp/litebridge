package org.litebridgedb.orm.api.register;

import org.litebridgedb.orm.api.spec.ColumnSpec;
import org.litebridgedb.orm.api.spec.DtoTableSpec;
import org.litebridgedb.orm.api.spec.FieldSpec;
import org.litebridgedb.orm.api.spec.TableMapping;

import java.util.function.Function;

public final class RegistrationJoinStep extends RegistrationTableContextImpl {

    private final FieldSpec fieldSpec;
    private final String column;
    private final String joinColumn;
    private final RegistrationTableContextImpl registrationTableContext;

    public RegistrationJoinStep(final FieldSpec fieldSpec,
                                final String column,
                                final String joinColumn,
                                final RegistrationTableContextImpl registrationTableContext) {
        super(registrationTableContext);
        this.fieldSpec = fieldSpec;
        this.column = column;
        this.joinColumn = joinColumn;
        this.registrationTableContext = registrationTableContext;
    }

    public RegistrationTableContext withMappedTable(final Class<?> dtoClass, final Function<RegistrationContext, RegistrationTableContext> rc) {
        final DtoTableSpecBuilder dtoTableSpecBuilder = (DtoTableSpecBuilder) rc.apply(new RegistrationContext(registrationTableContext.databaseProvider()));
        final DtoTableSpec dtoTableSpec = dtoTableSpecBuilder.buildDtoTableSpec(dtoClass);
        registrationTableContext.addFieldColumnMapping(fieldSpec, new ColumnSpec(column, null, joinColumn, new TableMapping(dtoClass, dtoTableSpec.tableSpec())));
        return registrationTableContext;
    }

    @Override
    public RegistrationFieldContext mapField(final String fieldName) {
        registrationTableContext.addFieldColumnMapping(fieldSpec, new ColumnSpec(column, null, joinColumn));
        return registrationTableContext.mapField(fieldName);
    }

    @Override
    public RegistrationFieldContext mapProperty(final String fieldName) {
        registrationTableContext.addFieldColumnMapping(fieldSpec, new ColumnSpec(column, null, joinColumn));
        return registrationTableContext.mapProperty(fieldName);
    }

    @Override
    public DtoTableSpec buildDtoTableSpec(final Class<?> dtoClass) {
        addFieldColumnMapping(fieldSpec, new ColumnSpec(column,  null, joinColumn));
        return super.buildDtoTableSpec(dtoClass);
    }
}
