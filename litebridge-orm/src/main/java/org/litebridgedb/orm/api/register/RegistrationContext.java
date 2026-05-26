package org.litebridgedb.orm.api.register;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.DatabaseProvider;

import java.util.ArrayList;
import java.util.List;

public final class RegistrationContext {

    private final Class<?> dtoClass;
    private final DatabaseProvider databaseProvider;
    private @Nullable List<Class<?>> dtoInterfaces;

    public RegistrationContext(final Class<?> dtoClass, final DatabaseProvider databaseProvider) {
        this.dtoClass = dtoClass;
        this.databaseProvider = databaseProvider;
    }

    public RegistrationContext allowInterface(final Class<?> dtoInterface) {
        if (dtoInterfaces == null) {
            dtoInterfaces = new ArrayList<>();
        }

        dtoInterfaces.add(dtoInterface);
        return this;
    }

    public RegistrationContextTerminal mapToTable(final String tableName) {
        return new RegistrationContextTerminal(dtoClass, tableName, databaseProvider, dtoInterfaces);
    }
}
