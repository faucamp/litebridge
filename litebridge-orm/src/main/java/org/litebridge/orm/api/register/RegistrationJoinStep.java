package org.litebridge.orm.api.register;

import org.litebridge.commons.BooleanUtils;
import org.litebridge.orm.api.spec.ColumnSpec;
import org.litebridge.orm.api.spec.FieldSpec;
import org.litebridge.orm.api.spec.TableMapping;
import org.litebridge.orm.api.spec.TableSpec;

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
        final RegistrationSpec registrationSpec = (RegistrationSpec) rc.apply(new RegistrationContext());
        registrationTableContext.addFieldColumnMapping(fieldSpec, new ColumnSpec(column, false, null, joinColumn, new TableMapping(dtoClass, registrationSpec.buildTableSpec())));
        return registrationTableContext;
    }

    @Override
    public RegistrationFieldContext mapField(final String fieldName) {
        registrationTableContext.addFieldColumnMapping(fieldSpec, new ColumnSpec(column, false, null, joinColumn));
        return registrationTableContext.mapField(fieldName);

    }

    @Override
    public RegistrationFieldContext mapProperty(final String fieldName) {
        registrationTableContext.addFieldColumnMapping(fieldSpec, new ColumnSpec(column, false, null, joinColumn));
        return registrationTableContext.mapProperty(fieldName);
    }

    @Override
    public TableSpec buildTableSpec() {
        addFieldColumnMapping(fieldSpec, new ColumnSpec(column, false, null, joinColumn));
        return super.buildTableSpec();
    }
}
