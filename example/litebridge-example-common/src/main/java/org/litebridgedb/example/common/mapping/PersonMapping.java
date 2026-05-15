package org.litebridgedb.example.common.mapping;

import org.litebridgedb.orm.api.spec.FieldColumnSpec;

import static org.litebridgedb.orm.api.spec.FieldColumnMapping.f;
import static org.litebridgedb.orm.api.spec.FieldColumnMapping.p;

public class PersonMapping {

    public static final FieldColumnSpec id = f("id")
            .c("PERSON_ID").autoIncrement().usingSequence("LB.PERSON_SEQ");

    public static final FieldColumnSpec name = f("name")
            .c("FIRST_NAME");

    public static final FieldColumnSpec surname = f("surname")
            .c("SURNAME");

    public static final FieldColumnSpec age = f("age")
            .c("AGE");

    public static final FieldColumnSpec eyeColour = p("eyeColour")
            .c("EYE_COLOUR");
}
