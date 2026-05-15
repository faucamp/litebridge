package org.litebridgedb.orm.api.spec;

import java.lang.invoke.MethodHandles;

public record TableMapping(MethodHandles.Lookup lookup, Class<?> dtoClass, TableSpec tableSpec) {

    public TableMapping(Class<?> dtoClass, TableSpec tableSpec) {
        this(MethodHandles.publicLookup(), dtoClass, tableSpec);
    }
}
