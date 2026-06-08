package org.litebridgedb.spring.boot.autoconfigure.test.mapping;

import org.litebridgedb.orm.api.register.TypeSafeDtoTableMapping;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;

public class TestScannedMapping extends TypeSafeDtoTableMapping {

    public static final FieldColumnSpec ID = field(fs -> fs.mapField("id").toColumn("ID"));

    @Override
    protected String table() {
        return "LB.TEST_SCANNED_DTO";
    }

    @Override
    protected Class<?> dtoClass() {
        return TestScannedDto.class;
    }
}