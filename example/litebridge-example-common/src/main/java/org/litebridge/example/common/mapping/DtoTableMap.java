package org.litebridge.example.common.mapping;

import org.litebridge.orm.api.spec.ColumnSpec;

import java.util.Map;

import static org.litebridge.orm.api.spec.ColumnSpecBuilder.c;

public final class DtoTableMap {

    private DtoTableMap() {}

    public static final Map<String, ColumnSpec> Person = Map.of(
            "id", c("PERSON_ID").autoIncrement(true).sequence("LB.PERSON_SEQ"),
            "name", c("FIRST_NAME"),
            "surname", c("SURNAME"),
            "age", c("AGE"),
            "eyeColour", c("EYE_COLOUR")
    );

    public static final Map<String, ColumnSpec> Account = Map.of(
            "id", c("ACCOUNT_ID").autoIncrement(true).sequence("LB.ACCOUNT_SEQ"),
            "name", c("ACCOUNT_NAME"),
            "owner", c("PERSON_ID").joinUsing()
    );
}
