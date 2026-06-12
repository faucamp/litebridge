package org.litebridgedb.spring.testmappings.two;

import org.litebridgedb.orm.api.register.TypeSafeDtoTableMapping;

public class PackageTwoMapping extends TypeSafeDtoTableMapping {

    @Override
    protected String table() {
        return "PACKAGE_TWO_MAPPING";
    }

    @Override
    protected Class<?> dtoClass() {
        return Object.class;
    }
}