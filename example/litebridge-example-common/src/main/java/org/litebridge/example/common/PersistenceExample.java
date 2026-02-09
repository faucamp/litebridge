package org.litebridge.example.common;

import org.litebridge.example.common.dto.Account;
import org.litebridge.example.common.dto.Person;
import org.litebridge.orm.Litebridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceExample extends AbstractExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceExample.class);

    public PersistenceExample(final Litebridge litebridge) {
        super(litebridge);
    }

    @Override
    public void run() {
        LOGGER.info("---======< Persistence example >======---");

        // Create DTOs and enable change tracking
        final Person person = litebridge.track(new Person());
        person.setName("Alice");
        person.setSurname("Smith");
        person.setAge(20);
        person.setEyeColour("blue");

        final Account account = litebridge.track(new Account());
        account.setName("Account 1");
        account.setOwner(person);

        LOGGER.info("[EXAMPLE] Save DTOs (\"person\" will also be saved due to cascading)");
        litebridge.save(account);
        LOGGER.info("Saved person ID: {}", person.getId());
        LOGGER.info("Saved account ID: {}", account.getId());

        LOGGER.info("[EXAMPLE] Add another account to the person");
        final Account account2 = litebridge.track(new Account());
        account2.setName("Account 2");
        account2.setOwner(person);
        litebridge.save(account2);

        LOGGER.info("[EXAMPLE] Update a single field of a tracked DTO and update the database");
        person.setEyeColour("brown");
        litebridge.save(person);
    }
}
