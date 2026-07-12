package org.litebridge.orm.e2e.setup;

import org.litebridge.orm.Litebridge;
import org.litebridge.orm.e2e.basic.dto.Account;
import org.litebridge.orm.e2e.basic.dto.Person;

/**
 * PostgreSQL-specific table mappings (lowercase)
 */
public class PostgresDbEnvDtoTableMapper implements DbEnvDtoTableMapper {

    @Override
    public String qualifyName(final String tableName) {
        return "lb." + tableName.toLowerCase();
    }

    @Override
    public String transformColumnName(final String columnName) {
        if ("COUNT(*)".equals(columnName)) {
            return "count";
        }

        return columnName.toLowerCase();
    }

    @Override
    public void registerPersonDtoTableMapping(final Litebridge litebridge) {
        litebridge.register(Person.class, rc -> rc.mapToTable("lb.person")
                .with(spec -> spec.mapField("id").toColumn("person_id").generateUsingSequence("lb.person_seq"))
                .with(spec -> spec.mapField("name").toColumn("first_name"))
                .with(spec -> spec.mapField("surname").toColumn("surname"))
                .with(spec -> spec.mapField("age").toColumn("age"))
                .with(spec -> spec.mapProperty("eyeColour").toColumn("eye_colour"))
                .with(spec -> spec.mapField("accounts").oneToMany(c -> c.mappedByField("owner")))
                .with(spec -> spec.mapField("addresses").oneToMany(c -> c.mappedByField("person"))));
    }

    @Override
    public void registerAccountDtoTableMapping(final Litebridge litebridge) {
        litebridge.register(Account.class, rc -> rc.mapToTable("lb.account")
                .with(spec -> spec.mapField("id").toColumn("account_id").generateUsingSequence("lb.account_seq"))
                .with(spec -> spec.mapField("name").toColumn("account_name"))
                .with(spec -> spec.mapField("balance").toColumn("balance"))
                .with(spec -> spec.mapField("owner").toColumn("person_id").joinUsing()));
    }
}
