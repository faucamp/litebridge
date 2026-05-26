package org.litebridgedb.orm.api.register;

import org.litebridgedb.orm.api.spec.DtoTableSpec;
import org.litebridgedb.orm.api.spec.TableSpec;

import java.util.Collections;

public class DtoTableSpecBuilder {

    private final RegistrationContextTerminal context;

    public DtoTableSpecBuilder(final RegistrationContextTerminal context) {
        this.context = context;
    }

    public DtoTableSpec build() {
        return new DtoTableSpec(context.dtoClass, new TableSpec(context.tableName, context.fieldColumnMap), context.dtoInterfaces != null ? context.dtoInterfaces : Collections.emptyList());
    }
}
