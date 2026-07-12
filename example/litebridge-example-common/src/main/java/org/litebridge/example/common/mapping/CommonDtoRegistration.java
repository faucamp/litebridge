package org.litebridge.example.common.mapping;

import org.litebridge.example.common.dto.Account;
import org.litebridge.example.common.dto.Person;
import org.litebridge.orm.Litebridge;

public class CommonDtoRegistration {

    public static void registerPersonAndAccount(final Litebridge litebridge) {
        litebridge.register(Person.class, rc -> rc.mapToTable("LB.PERSON")
                .with(spec -> spec.mapField("id").toColumn("PERSON_ID").generateUsingSequence("LB.PERSON_SEQ"))
                .with(spec -> spec.mapField("name").toColumn("FIRST_NAME"))
                .with(spec -> spec.mapField("surname").toColumn("SURNAME"))
                .with(spec -> spec.mapField("age").toColumn("AGE"))
                .with(spec -> spec.mapProperty("eyeColour").toColumn("EYE_COLOUR")));

        litebridge.register(Account.class, rc -> rc.mapToTable("LB.ACCOUNT")
                .with(spec -> spec.mapField("id").toColumn("ACCOUNT_ID").generateUsingSequence("LB.ACCOUNT_SEQ"))
                .with(spec -> spec.mapField("name").toColumn("ACCOUNT_NAME"))
                .with(spec -> spec.mapField("owner").toColumn("PERSON_ID").joinUsing()));
    }
}
