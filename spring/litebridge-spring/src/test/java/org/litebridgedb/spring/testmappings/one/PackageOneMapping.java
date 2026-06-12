package org.litebridgedb.spring.testmappings.one;

import org.litebridgedb.orm.api.register.TypeSafeDtoTableMapping;

public class PackageOneMapping extends TypeSafeDtoTableMapping {

    @Override
    protected String table() {
        return "PACKAGE_ONE_MAPPING";
    }

    @Override
    protected Class<?> dtoClass() {
        return Object.class;
    }
}