package org.litebridgedb.orm.api.register;

import org.litebridgedb.orm.api.spec.DtoTableSpec;

public sealed interface DtoTableSpecBuilder permits RegistrationTableContextImpl {

    DtoTableSpec buildDtoTableSpec(final Class<?> dtoClass);
}
