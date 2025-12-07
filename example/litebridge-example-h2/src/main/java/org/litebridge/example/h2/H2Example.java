package org.litebridge.example.h2;

import org.litebridge.core.LiteBridge;
import org.litebridge.db.api.DatabaseProvider;
import org.litebridge.db.h2.H2DatabaseProvider;
import org.litebridge.example.common.dto.Account;
import org.litebridge.example.common.dto.Person;
import org.litebridge.example.common.mapping.DtoTableMap;

import java.sql.Connection;
import java.sql.DriverManager;

public class H2Example {

    public static void main(String[] args) {
        final String userName = "sa";
        final String password = "";
        final String url = "jdbc:h2:file:./target/h2/lb";

        try (Connection connection = DriverManager.getConnection(url, userName, password)) {

            final DatabaseProvider databaseProvider = new H2DatabaseProvider(connection);
            final LiteBridge liteBridge = new LiteBridge(databaseProvider);

            liteBridge.register(Person.class, null, "LB", "PERSON", DtoTableMap.Person);
            liteBridge.register(Account.class, null, "LB", "ACCOUNT", DtoTableMap.Account);

            final Person person = new Person();
            person.setId(123L);
            person.setName("Alice");
            person.setSurname("Smith");
            person.setAge(20);
            person.setEyeColour("blue");

            final Account account = new Account();

            liteBridge.track(person);
            liteBridge.track(account);

            person.setEyeColour("brown");
            account.setId(234L);
            account.setName("Test account");
            account.setOwner(person);

            liteBridge.save(person);
            liteBridge.save(account);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
