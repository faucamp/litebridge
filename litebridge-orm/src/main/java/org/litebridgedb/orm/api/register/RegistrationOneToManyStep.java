package org.litebridgedb.orm.api.register;

import org.litebridgedb.orm.api.spec.FieldSpec;
import org.litebridgedb.orm.api.spec.OneToMany;

public class RegistrationOneToManyStep {

    private final FieldSpec fieldSpec;
    private final RegistrationTableContextImpl registrationTableContext;

    RegistrationOneToManyStep(final FieldSpec fieldSpec, final RegistrationTableContextImpl registrationTableContext) {
        this.fieldSpec = fieldSpec;
        this.registrationTableContext = registrationTableContext;
    }

    public RegistrationTableContext mappedByField(final String field) {
        registrationTableContext.addFieldColumnMapping(fieldSpec, new OneToMany(new FieldSpec(field, false)));
        return registrationTableContext;
    }

    public RegistrationTableContext mappedByProperty(final String property) {
        registrationTableContext.addFieldColumnMapping(fieldSpec, new OneToMany(new FieldSpec(property, true)));
        return registrationTableContext;
    }
}
