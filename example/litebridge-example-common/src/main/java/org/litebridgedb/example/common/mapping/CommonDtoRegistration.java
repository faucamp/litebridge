package org.litebridgedb.example.common.mapping;

import org.litebridgedb.example.common.dto.Account;
import org.litebridgedb.example.common.dto.Person;
import org.litebridgedb.orm.Litebridge;

public class CommonDtoRegistration {

    public static void registerPersonAndAccount(final Litebridge litebridge) {
        litebridge.register(Person.class, rc -> rc.mapToTable("LB.PERSON")
                .mapField("id").toColumn("PERSON_ID").generateUsingSequence("LB.PERSON_SEQ")
                .mapField("name").toColumn("FIRST_NAME")
                .mapField("surname").toColumn("SURNAME")
                .mapField("age").toColumn("AGE")
                .mapProperty("eyeColour").toColumn("EYE_COLOUR"));

        litebridge.register(Account.class, rc -> rc.mapToTable("LB.ACCOUNT")
                .mapField("id").toColumn("ACCOUNT_ID").generateUsingSequence("LB.ACCOUNT_SEQ")
                .mapField("name").toColumn("ACCOUNT_NAME")
                .mapField("owner").toColumn("PERSON_ID").joinUsing());
    }
}
