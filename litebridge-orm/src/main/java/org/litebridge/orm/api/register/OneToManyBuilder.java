package org.litebridge.orm.api.register;

import org.litebridge.orm.api.spec.FieldSpec;
import org.litebridge.orm.api.spec.OneToMany;

public class OneToManyBuilder {

    public OneToMany mappedByField(final String field) {
        return new OneToMany(new FieldSpec(field, false));
    }

    public OneToMany mappedByProperty(final String property) {
        return new OneToMany(new FieldSpec(property, true));
    }
}
