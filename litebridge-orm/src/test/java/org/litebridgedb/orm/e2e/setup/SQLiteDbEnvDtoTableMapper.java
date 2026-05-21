package org.litebridgedb.orm.e2e.setup;

import org.litebridgedb.orm.Litebridge;
import org.litebridgedb.orm.e2e.basic.dto.Account;
import org.litebridgedb.orm.e2e.basic.dto.Person;
import org.litebridgedb.orm.e2e.manytomany.dto.Group;
import org.litebridgedb.orm.e2e.manytomany.dto.GroupedPerson;

/**
 * SQLite-specific table mappings (no sequences)
 */
public class SQLiteDbEnvDtoTableMapper implements DbEnvDtoTableMapper {

    @Override
    public void registerPersonDtoTableMapping(final Litebridge litebridge) {
        litebridge.register(Person.class, rc -> rc.mapToTable("LB.PERSON")
                .mapField("id").toColumn("PERSON_ID").autoIncrement()
                .mapField("name").toColumn("FIRST_NAME")
                .mapField("surname").toColumn("SURNAME")
                .mapField("age").toColumn("AGE")
                .mapProperty("eyeColour").toColumn("EYE_COLOUR")
                .mapField("accounts").oneToMany(c -> c.mappedByField("owner")));
    }

    @Override
    public void registerAccountDtoTableMapping(final Litebridge litebridge) {
        litebridge.register(Account.class, rc -> rc.mapToTable("LB.ACCOUNT")
                .mapField("id").toColumn("ACCOUNT_ID").autoIncrement()
                .mapField("name").toColumn("ACCOUNT_NAME")
                .mapField("balance").toColumn("BALANCE")
                .mapField("owner").toColumn("PERSON_ID").joinUsing());
    }

    @Override
    public void registerGroupedPersonDtoTableMapping(final Litebridge litebridge) {
        litebridge.register(GroupedPerson.class, rc -> rc
                .allowInterface(Person.class)
                .mapToTable("LB.PERSON")
                .mapField("id").toColumn("PERSON_ID").autoIncrement()
                .mapField("name").toColumn("FIRST_NAME")
                .mapField("groups").manyToMany(c -> c.joinTable("LB.PERSON_GROUP")
                        .joinColumn("PERSON_ID")
                        .inverseJoinColumn("GROUP_NAME")));
    }

    @Override
    public void registerGroupDtoTableMapping(final Litebridge litebridge) {
        litebridge.register(Group.class, rc -> rc.mapToTable("LB.GROUP")
                .mapField("name").toColumn("GROUP_NAME")
                .mapField("description").toColumn("GROUP_DESC")
                .mapField("members").manyToMany(c -> c.joinTable("LB.PERSON_GROUP")
                        .joinColumn("GROUP_NAME")
                        .inverseJoinColumn("PERSON_ID")));
    }
}
