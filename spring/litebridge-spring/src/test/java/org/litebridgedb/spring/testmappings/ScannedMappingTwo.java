package org.litebridgedb.spring.testmappings;

import org.litebridgedb.orm.api.register.TypeSafeDtoTableMapping;

public class ScannedMappingTwo extends TypeSafeDtoTableMapping {

    @Override
    protected String table() {
        return "SCANNED_MAPPING_TWO";
    }

    @Override
    protected Class<?> dtoClass() {
        return Object.class;
    }
}