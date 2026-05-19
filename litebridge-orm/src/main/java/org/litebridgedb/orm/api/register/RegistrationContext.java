package org.litebridgedb.orm.api.register;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.DatabaseProvider;

import java.util.ArrayList;
import java.util.List;

public final class RegistrationContext {

    private final DatabaseProvider databaseProvider;
    private @Nullable List<Class<?>> dtoInterfaces;

    public RegistrationContext(final DatabaseProvider databaseProvider) {
        this.databaseProvider = databaseProvider;
    }

    public RegistrationContext allowInterface(final Class<?> dtoInterface) {
        if (dtoInterfaces == null) {
            dtoInterfaces = new ArrayList<>();
        }

        dtoInterfaces.add(dtoInterface);
        return this;
    }

    public RegistrationTableContext mapToTable(final String tableName) {
        return new RegistrationTableContextImpl(tableName, databaseProvider, dtoInterfaces);
    }
}
