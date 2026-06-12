package org.litebridgedb.spring.testmappings;

import org.litebridgedb.orm.api.register.TypeSafeDtoTableMapping;

public class ScannedMappingOne extends TypeSafeDtoTableMapping {

    @Override
    protected String table() {
        return "SCANNED_MAPPING_ONE";
    }

    @Override
    protected Class<?> dtoClass() {
        return Object.class;
    }
}