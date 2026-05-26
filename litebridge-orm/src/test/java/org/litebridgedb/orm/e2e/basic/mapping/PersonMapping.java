package org.litebridgedb.orm.e2e.basic.mapping;

import org.litebridgedb.orm.api.register.TypeSafeDtoTableMapping;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.e2e.basic.dto.Person;

public final class PersonMapping extends TypeSafeDtoTableMapping {

    public static final FieldColumnSpec id = field(rc -> rc.mapField("id").toColumn("PERSON_ID").generateUsingSequence("LB.PERSON_SEQ"));
    public static final FieldColumnSpec name = field(rc -> rc.mapField("name").toColumn("FIRST_NAME"));
    public static final FieldColumnSpec surname = field(rc -> rc.mapField("surname").toColumn("SURNAME"));
    public static final FieldColumnSpec age = field(rc -> rc.mapField("age").toColumn("AGE"));
    public static final FieldColumnSpec eyeColour = field(rc -> rc.mapProperty("eyeColour").toColumn("EYE_COLOUR"));
    public static final FieldColumnSpec accounts = field(rc -> rc.mapField("accounts").oneToMany(c -> c.mappedByField("owner")));

    @Override
    protected String table() {
        return "LB.PERSON";
    }

    @Override
    protected Class<?> dtoClass() {
        return Person.class;
    }
}
