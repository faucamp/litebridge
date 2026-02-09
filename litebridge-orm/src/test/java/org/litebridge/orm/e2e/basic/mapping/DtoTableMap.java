package org.litebridge.orm.e2e.basic.mapping;

import org.litebridge.orm.api.spec.ColumnMapping;
import org.litebridge.orm.api.spec.FieldSpec;

import java.util.Map;

import static org.litebridge.orm.api.spec.ColumnMapping.c;
import static org.litebridge.orm.api.spec.ColumnMapping.oneToMany;
import static org.litebridge.orm.api.spec.FieldMapping.f;
import static org.litebridge.orm.api.spec.FieldMapping.p;

public final class DtoTableMap {

    private DtoTableMap() {
    }

    public static final Map<FieldSpec, ColumnMapping> Person = Map.of(
            f("id"), c("PERSON_ID").autoIncrement().usingSequence("LB.PERSON_SEQ"),
            f("name"), c("FIRST_NAME"),
            f("surname"), c("SURNAME"),
            f("age"), c("AGE"),
            p("eyeColour"), c("EYE_COLOUR"),
            f("accounts"), oneToMany("owner")
    );

    public static final Map<FieldSpec, ColumnMapping> Account = Map.of(
            f("id"), c("ACCOUNT_ID").autoIncrement().usingSequence("LB.ACCOUNT_SEQ"),
            f("name"), c("ACCOUNT_NAME"),
            f("balance"), c("BALANCE"),
            f("owner"), c("PERSON_ID").joinUsing()
    );
}
