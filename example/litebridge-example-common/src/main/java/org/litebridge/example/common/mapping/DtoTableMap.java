package org.litebridge.example.common.mapping;

import java.util.Map;

public final class DtoTableMap {

    private DtoTableMap() {}

    public static final Map<String, String> Person = Map.of(
            "id", "PERSON_ID",
            "name", "FIRST_NAME",
            "surname", "SURNAME",
            "age", "AGE",
            "eyeColour", "EYE_COLOUR"
    );

    public static final Map<String, String> Account = Map.of(
            "id", "ACCOUNT_ID",
            "name", "ACCOUNT_NAME",
            "owner", "PERSON_ID"
    );
}
