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
                .mapField("id").toColumn("PERSON_ID").autoIncrement()
                .mapField("name").toColumn("FIRST_NAME")
                .mapField("surname").toColumn("SURNAME")
                .mapField("age").toColumn("AGE")
                .mapProperty("eyeColour").toColumn("EYE_COLOUR")
                .mapField("accounts").oneToMany(c -> c.mappedByField("owner")));
    }

    @Override
    public void registerAccountDtoTableMapping(final Litebridge litebridge) {
        litebridge.register(Account.class, rc -> rc.mapToTable("ACCOUNT")
                .mapField("id").toColumn("ACCOUNT_ID").autoIncrement()
                .mapField("name").toColumn("ACCOUNT_NAME")
                .mapField("balance").toColumn("BALANCE")
                .mapField("owner").toColumn("PERSON_ID").joinUsing());
    }
}
