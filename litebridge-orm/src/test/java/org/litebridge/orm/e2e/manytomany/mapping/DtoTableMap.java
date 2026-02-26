package org.litebridge.orm.e2e.manytomany.mapping;

import org.litebridge.orm.api.spec.ColumnMapping;
import org.litebridge.orm.api.spec.FieldMapping;

import java.util.Map;

import static org.litebridge.orm.api.spec.ColumnMapping.c;
import static org.litebridge.orm.api.spec.ColumnMapping.manyToMany;
import static org.litebridge.orm.api.spec.FieldMapping.f;

public final class DtoTableMap {

    private DtoTableMap() {
    }

    public static final Map<FieldMapping, ColumnMapping> GroupedPerson = Map.of(
            f("id"), c("PERSON_ID").autoIncrement().usingSequence("LB.PERSON_SEQ"),
            f("name"), c("FIRST_NAME"),
            f("groups"), manyToMany("LB.PERSON_GROUP", "PERSON_ID", "GROUP_NAME")
    );

    public static final Map<FieldMapping, ColumnMapping> Group = Map.of(
            f("name"), c("GROUP_NAME"),
            f("description"), c("GROUP_DESC"),
            f("members"), manyToMany("LB.PERSON_GROUP", "GROUP_NAME", "PERSON_ID")
    );
}
