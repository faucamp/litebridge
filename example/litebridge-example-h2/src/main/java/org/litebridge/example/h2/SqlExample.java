package org.litebridge.example.h2;

import org.litebridge.orm.Litebridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlExample extends AbstractExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlExample.class);

    public SqlExample(final Litebridge litebridge) {
        super(litebridge);
    }

    @Override
    public void run() {
        // Retrieve a person's details using a lower-level SQL query
        litebridge.select("FIRST_NAME", "SURNAME", "AGE").from("LB", "PERSON")
                .where("AGE").gt(18)
                .and("AGE").lt(25)
                .stream()
                .forEach(record -> LOGGER.info("SQL result: Selected data for PERSON record: " + record));

        // Retrieve a person's details using a lower-level SQL query and map it to a DTO
        litebridge.select("FIRST_NAME", "SURNAME", "AGE").from("LB", "PERSON")
                .where("AGE").gt(18)
                .and("AGE")
                .lt(25)
//                    .toDto(Person.class)
                .stream()
                .forEach(p -> LOGGER.info("SQL result: Selected data for PERSON record: " + p));
    }

}
