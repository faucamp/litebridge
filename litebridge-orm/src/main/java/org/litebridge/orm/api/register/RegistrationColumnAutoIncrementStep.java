package org.litebridge.orm.api.register;

import org.litebridge.orm.api.spec.ColumnSpec;
import org.litebridge.orm.api.spec.FieldSpec;

public final class RegistrationColumnAutoIncrementStep {

    private final FieldSpec fieldSpec;
    private final String column;
    private final RegistrationTableContextImpl registrationTableContext;

    RegistrationColumnAutoIncrementStep(final FieldSpec fieldSpec, final String column, final RegistrationTableContextImpl registrationTableContext) {
        this.fieldSpec = fieldSpec;
        this.column = column;
        this.registrationTableContext = registrationTableContext;
    }

    public RegistrationTableContext usingSequence(final String sequence) {
        registrationTableContext.addFieldColumnMapping(fieldSpec, new ColumnSpec(column, true, sequence, null));
        return registrationTableContext;
    }

    public RegistrationTableContext natively() {
        registrationTableContext.addFieldColumnMapping(fieldSpec, new ColumnSpec(column, true, null, null));
        return registrationTableContext;
    }
}
