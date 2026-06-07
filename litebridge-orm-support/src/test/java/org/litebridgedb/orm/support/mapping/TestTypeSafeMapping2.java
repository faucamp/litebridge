package org.litebridgedb.orm.support.mapping;

import org.litebridgedb.orm.api.register.TypeSafeDtoTableMapping;

public class TestTypeSafeMapping2 extends TypeSafeDtoTableMapping {
    @Override
    protected String table() {
        return "TEST2";
    }

    @Override
    protected Class<?> dtoClass() {
        return String.class;
    }
}
