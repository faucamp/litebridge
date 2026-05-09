package org.litebridge.example.common.mapping;

import org.litebridge.example.common.dto.Account;
import org.litebridge.example.common.dto.Person;
import org.litebridge.orm.Litebridge;

import java.sql.SQLException;

public class CommonDtoRegistration {

    public static void registerPersonAndAccount(final Litebridge litebridge) {
        litebridge.register(Person.class, rc -> rc.mapToTable("LB.PERSON")
                .mapField("id").toColumn("PERSON_ID").autoIncrement().usingSequence("LB.PERSON_SEQ")
                .mapField("name").toColumn("FIRST_NAME")
                .mapField("surname").toColumn("SURNAME")
                .mapField("age").toColumn("AGE")
                .mapProperty("eyeColour").toColumn("EYE_COLOUR"));

        litebridge.register(Account.class, rc -> rc.mapToTable("LB.ACCOUNT")
                .mapField("id").toColumn("ACCOUNT_ID").autoIncrement().usingSequence("LB.ACCOUNT_SEQ")
                .mapField("name").toColumn("ACCOUNT_NAME")
                .mapField("owner").toColumn("PERSON_ID").joinUsing());
    }
}
