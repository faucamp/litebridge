package org.litebridgedb.orm.e2e.setup;

import org.litebridgedb.orm.Litebridge;
import org.litebridgedb.orm.e2e.basic.dto.Account;
import org.litebridgedb.orm.e2e.basic.dto.Person;
import org.litebridgedb.orm.e2e.basic.mapping.AccountMapping;
import org.litebridgedb.orm.e2e.basic.mapping.PersonMapping;

/**
 * Default DTO-table regisration mapping
 */
public class DefaultDtoTableMapper implements DbEnvDtoTableMapper {

    @Override
    public String qualifyName(final String tableName) {
        return "LB." + tableName;
    }

    @Override
    public void registerPersonDtoTableMapping(final Litebridge litebridge, final boolean typeSafe) {
        if (typeSafe) {
            litebridge.register(new PersonMapping());
        } else {
            litebridge.register(Person.class, rc -> rc.mapToTable("LB.PERSON")
                    .with(spec -> spec.mapField("id").toColumn("PERSON_ID").generateUsingSequence("LB.PERSON_SEQ"))
                    .with(spec -> spec.mapField("name").toColumn("FIRST_NAME"))
                    .with(spec -> spec.mapField("surname").toColumn("SURNAME"))
                    .with(spec -> spec.mapField("age").toColumn("AGE"))
                    .with(spec -> spec.mapProperty("eyeColour").toColumn("EYE_COLOUR"))
                    .with(spec -> spec.mapField("accounts").oneToMany(c -> c.mappedByField("owner")))
                    .with(spec -> spec.mapField("addresses").oneToMany(c -> c.mappedByField("person"))));
        }
    }

    @Override
    public void registerAccountDtoTableMapping(final Litebridge litebridge, final boolean typeSafe) {
        if (typeSafe) {
            litebridge.register(new AccountMapping());
        } else {
            litebridge.register(Account.class, rc -> rc.mapToTable("LB.ACCOUNT")
                    .with(spec -> spec.mapField("id").toColumn("ACCOUNT_ID").generateUsingSequence("LB.ACCOUNT_SEQ"))
                    .with(spec -> spec.mapField("name").toColumn("ACCOUNT_NAME"))
                    .with(spec -> spec.mapField("balance").toColumn("BALANCE"))
                    .with(spec -> spec.mapField("owner").toColumn("PERSON_ID").joinUsing()));
        }
    }
}
