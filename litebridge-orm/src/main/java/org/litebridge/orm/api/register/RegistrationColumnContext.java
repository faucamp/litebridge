package org.litebridge.orm.api.register;

import org.litebridge.orm.api.spec.ColumnSpec;
import org.litebridge.orm.api.spec.FieldSpec;
import org.litebridge.orm.api.spec.TableSpec;

public final class RegistrationColumnContext extends RegistrationTableContextImpl {

    private final FieldSpec fieldSpec;
    private final String column;
    private final RegistrationTableContextImpl registrationTableContext;

    RegistrationColumnContext(final FieldSpec fieldSpec, final String column, final RegistrationTableContextImpl registrationTableContext) {
        super(registrationTableContext);
        this.fieldSpec = fieldSpec;
        this.column = column;
        this.registrationTableContext = registrationTableContext;
    }

    public RegistrationColumnAutoIncrementStep autoIncrement() {
        return new RegistrationColumnAutoIncrementStep(fieldSpec, column, registrationTableContext);
    }

    public RegistrationJoinStep joinOn(final String column) {
        return new RegistrationJoinStep(fieldSpec, this.column, column, registrationTableContext);
    }

    public RegistrationJoinStep joinUsing() {
        return joinOn(this.column);
    }

    @Override
    public RegistrationFieldContext mapField(final String fieldName) {
        registrationTableContext.addFieldColumnMapping(fieldSpec, new ColumnSpec(column, false, null, null));
        return registrationTableContext.mapField(fieldName);
    }

    @Override
    public RegistrationFieldContext mapProperty(final String fieldName) {
        registrationTableContext.addFieldColumnMapping(fieldSpec, new ColumnSpec(column, false, null, null));
        return registrationTableContext.mapProperty(fieldName);
    }

    @Override
    public TableSpec buildTableSpec() {
        addFieldColumnMapping(fieldSpec, new ColumnSpec(column, false, null, null));
        return super.buildTableSpec();
    }
}
