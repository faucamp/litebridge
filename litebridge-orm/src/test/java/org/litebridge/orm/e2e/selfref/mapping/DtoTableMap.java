package org.litebridge.orm.e2e.selfref.mapping;

import org.litebridge.orm.api.spec.ColumnMapping;
import org.litebridge.orm.api.spec.FieldMapping;
import org.litebridge.orm.api.spec.FieldSpec;

import java.util.Map;

import static org.litebridge.orm.api.spec.ColumnMapping.c;
import static org.litebridge.orm.api.spec.FieldMapping.f;

public final class DtoTableMap {

    private DtoTableMap() {
    }

    public static final Map<FieldMapping, ColumnMapping> SelfReferencingDto = Map.of(
            f("id"), c("ID"),
            f("myVar"), c("MY_VAR"),
            f("parent"), c("PARENT_ID").joinOn("ID")
    );
}
