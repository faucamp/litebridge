package org.litebridge.orm.api.register;

public sealed interface RegistrationTableContext permits RegistrationTableContextImpl {

    RegistrationFieldContext mapField(String fieldName);

    RegistrationFieldContext mapProperty(String fieldName);
}
