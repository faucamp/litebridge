package org.litebridge.orm.e2e.mapping;

import org.litebridge.orm.api.spec.FieldColumnSpec;

import static org.litebridge.orm.api.spec.FieldSpecBuilder.f;
import static org.litebridge.orm.api.spec.FieldSpecBuilder.p;

public class PersonMapping {

    public static final FieldColumnSpec id = f("id")
            .c("PERSON_ID").autoIncrement(true).sequence("LB.PERSON_SEQ");

    public static final FieldColumnSpec name = f("name")
            .c("FIRST_NAME");

    public static final FieldColumnSpec surname = f("surname")
            .c("SURNAME");

    public static final FieldColumnSpec age = f("age")
            .c("AGE");

    public static final FieldColumnSpec eyeColour = p("eyeColour")
            .c("EYE_COLOUR");
}
