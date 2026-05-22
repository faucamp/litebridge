package org.litebridgedb.orm.e2e.setup;

import org.litebridgedb.orm.Litebridge;
import org.litebridgedb.orm.e2e.basic.dto.Account;
import org.litebridgedb.orm.e2e.basic.dto.Person;

/**
 * Default DTO-table regisration mapping
 */
public class DefaultDtoTableMapper implements DbEnvDtoTableMapper {

    @Override
    public String qualifyName(final String tableName) {
        return "LB." + tableName;
    }

    @Override
    public void registerPersonDtoTableMapping(final Litebridge litebridge) {
        litebridge.register(Person.class, rc -> rc.mapToTable("LB.PERSON")
                .mapField("id").toColumn("PERSON_ID").generateUsingSequence("LB.PERSON_SEQ")
                .mapField("name").toColumn("FIRST_NAME")
                .mapField("surname").toColumn("SURNAME")
                .mapField("age").toColumn("AGE")
                .mapProperty("eyeColour").toColumn("EYE_COLOUR")
                .mapField("accounts").oneToMany(c -> c.mappedByField("owner")));
    }

    @Override
    public void registerAccountDtoTableMapping(final Litebridge litebridge) {
        litebridge.register(Account.class, rc -> rc.mapToTable("LB.ACCOUNT")
                .mapField("id").toColumn("ACCOUNT_ID").generateUsingSequence("LB.ACCOUNT_SEQ")
                .mapField("name").toColumn("ACCOUNT_NAME")
                .mapField("balance").toColumn("BALANCE")
                .mapField("owner").toColumn("PERSON_ID").joinUsing());
    }
}
