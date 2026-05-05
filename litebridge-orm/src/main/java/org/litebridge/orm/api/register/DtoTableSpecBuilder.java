package org.litebridge.orm.api.register;

import org.litebridge.orm.api.spec.DtoTableSpec;

public sealed interface DtoTableSpecBuilder permits RegistrationTableContextImpl {

    DtoTableSpec buildDtoTableSpec(final Class<?> dtoClass);
}
