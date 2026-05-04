package org.litebridge.orm.api.register;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.spec.TableSpec;

import java.util.List;

public sealed interface RegistrationSpec permits RegistrationTableContextImpl {

    TableSpec buildTableSpec();

    @Nullable List<Class<?>> dtoInterfaces();
}
