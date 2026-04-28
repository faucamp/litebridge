package org.litebridge.orm.e2e.compositepk.mapping;

import org.litebridge.orm.api.spec.ColumnMapping;
import org.litebridge.orm.api.spec.FieldMapping;

import java.util.Map;

import static org.litebridge.orm.api.spec.ColumnMapping.c;
import static org.litebridge.orm.api.spec.FieldMapping.f;

public final class DtoTableMap {

    private DtoTableMap() {
    }

    public static final Map<FieldMapping, ColumnMapping> CompositePkLookup = Map.of(
            f("id"), c("LOOKUP_ID"),
            f("name"), c("LOOKUP_NAME")
    );

    public static final Map<FieldMapping, ColumnMapping> CompositePkFkTest = Map.of(
            f("lookup"), c("LOOKUP_ID").joinUsing(),
            f("testId"), c("TEST_ID"),
            f("description"), c("TEST_DESC")
    );

    public static final Map<FieldMapping, ColumnMapping> CompositePkSimple = Map.of(
            f("pk1"), c("PK1").autoIncrement().usingSequence("LB.COMPOSITE_PK1_SEQ"),
            f("pk2"), c("PK2").autoIncrement().usingSequence("LB.COMPOSITE_PK2_SEQ"),
            f("description"), c("TEST_DESC")
    );
}
