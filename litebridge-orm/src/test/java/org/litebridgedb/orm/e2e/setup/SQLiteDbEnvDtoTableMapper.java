package org.litebridgedb.orm.e2e.setup;

import org.litebridgedb.orm.Litebridge;
import org.litebridgedb.orm.e2e.basic.dto.Account;
import org.litebridgedb.orm.e2e.basic.dto.Person;

/**
 * SQLite-specific table mappings (no sequences)
 */
public class SQLiteDbEnvDtoTableMapper implements DbEnvDtoTableMapper {

    @Override
    public String qualifyName(final String tableName) {
        return tableName;
    }

    @Override
    public void registerPersonDtoTableMapping(final Litebridge litebridge) {
        litebridge.register(Person.class, rc -> rc.mapToTable("PERSON")
                .with(spec -> spec.mapField("id").toColumn("PERSON_ID"))
                .with(spec -> spec.mapField("name").toColumn("FIRST_NAME"))
                .with(spec -> spec.mapField("surname").toColumn("SURNAME"))
                .with(spec -> spec.mapField("age").toColumn("AGE"))
                .with(spec -> spec.mapProperty("eyeColour").toColumn("EYE_COLOUR"))
                .with(spec -> spec.mapField("accounts").oneToMany(c -> c.mappedByField("owner")))
                .with(spec -> spec.mapField("addresses").oneToMany(c -> c.mappedByField("person"))));
    }

    @Override
    public void registerAccountDtoTableMapping(final Litebridge litebridge) {
        litebridge.register(Account.class, rc -> rc.mapToTable("ACCOUNT")
                .with(spec -> spec.mapField("id").toColumn("ACCOUNT_ID"))
                .with(spec -> spec.mapField("name").toColumn("ACCOUNT_NAME"))
                .with(spec -> spec.mapField("balance").toColumn("BALANCE"))
                .with(spec -> spec.mapField("owner").toColumn("PERSON_ID").joinUsing()));
    }
}
