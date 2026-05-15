package org.litebridgedb.orm.e2e.singletable_multidto.mapping;

import org.litebridgedb.orm.api.spec.ColumnMapping;
import org.litebridgedb.orm.api.spec.FieldMapping;

import java.util.Map;

import static org.litebridgedb.orm.api.spec.ColumnMapping.c;
import static org.litebridgedb.orm.api.spec.FieldMapping.f;

public final class DtoTableMap {

    private DtoTableMap() {
    }

    public static final Map<FieldMapping, ColumnMapping> SingeTableNestedDto = Map.of(
            f("parentValue1"), c("PARENT_VALUE1"),
            f("nestedChild.childValue1"), c("CHILD_VALUE1"),
            f("nestedChild.grandChild.grandChildValue1"), c("GRANDCHILD_VALUE1")
    );
}
