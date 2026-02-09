package org.litebridge.orm.e2e.singletable_multidto.mapping;

import org.litebridge.orm.api.spec.ColumnMapping;
import org.litebridge.orm.api.spec.FieldSpec;

import java.util.Map;

import static org.litebridge.orm.api.spec.ColumnMapping.c;
import static org.litebridge.orm.api.spec.FieldMapping.f;

public final class DtoTableMap {

    private DtoTableMap() {
    }

    public static final Map<FieldSpec, ColumnMapping> SingeTableNestedDto = Map.of(
            f("parentValue1"), c("PARENT_VALUE1"),
            f("nestedChild.childValue1"), c("CHILD_VALUE1"),
            f("nestedChild.grandChild.grandChildValue1"), c("GRANDCHILD_VALUE1")
    );
}
