package org.litebridge.orm.api.register;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class RegistrationContext {

    private @Nullable List<Class<?>> dtoInterfaces;

    public RegistrationContext allowInterface(final Class<?> dtoInterface) {
        if (dtoInterfaces == null) {
            dtoInterfaces = new ArrayList<>();
        }

        dtoInterfaces.add(dtoInterface);
        return this;
    }

    public RegistrationTableContext mapToTable(final String tableName) {
        return new RegistrationTableContextImpl(tableName, dtoInterfaces);
    }
}
