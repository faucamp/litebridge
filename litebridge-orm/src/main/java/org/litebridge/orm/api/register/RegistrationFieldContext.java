package org.litebridge.orm.api.register;

import org.litebridge.orm.api.spec.FieldSpec;
import org.litebridge.orm.api.spec.ManyToMany;
import org.litebridge.orm.api.spec.OneToMany;

import java.util.function.Function;

public final class RegistrationFieldContext {

    private final FieldSpec fieldSpec;
    private final RegistrationTableContextImpl registrationTableContext;

    RegistrationFieldContext(final FieldSpec fieldSpec, final RegistrationTableContextImpl registrationTableContext) {
        this.fieldSpec = fieldSpec;
        this.registrationTableContext = registrationTableContext;
    }

    public RegistrationColumnContext toColumn(final String column) {
        return new RegistrationColumnContext(fieldSpec, column, registrationTableContext);
    }

    public RegistrationTableContext oneToMany(Function<OneToManyBuilder, OneToMany> c) {
        registrationTableContext.addFieldColumnMapping(fieldSpec, c.apply(new OneToManyBuilder()));
        return registrationTableContext;
    }

    public RegistrationTableContext manyToMany(Function<ManyToManyBuilder, ManyToMany> c) {
        registrationTableContext.addFieldColumnMapping(fieldSpec, c.apply(new ManyToManyBuilder()));
        return registrationTableContext;
    }
}
