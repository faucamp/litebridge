package org.litebridge.example.common;

import org.litebridge.example.common.dto.Person;
import org.litebridge.example.common.mapping.PersonMapping;
import org.litebridge.orm.Litebridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeSafeExample extends AbstractExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeSafeExample.class);

    public TypeSafeExample(final Litebridge litebridge) {
        super(litebridge);
    }

    @Override
    public void run() {
        LOGGER.info("---======< Type-safe example >======---");

        LOGGER.info("[EXAMPLE] Retrieve a single person with criteria");
        final Person alice = litebridge.select(Person.class)
                .where(PersonMapping.name).eq("Alice")
                .and(PersonMapping.surname).eq("Smith")
                .orderBy(PersonMapping.id).asc()
                .oneOrNull();
        LOGGER.info("Retrieved person (nullable): " + alice);

    }
}
