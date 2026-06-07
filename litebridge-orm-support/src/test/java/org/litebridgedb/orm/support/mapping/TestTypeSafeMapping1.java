package org.litebridgedb.orm.support.mapping;

import org.litebridgedb.orm.api.register.TypeSafeDtoTableMapping;

public class TestTypeSafeMapping1 extends TypeSafeDtoTableMapping {
    @Override
    protected String table() {
        return "TEST1";
    }

    @Override
    protected Class<?> dtoClass() {
        return Object.class;
    }
}
