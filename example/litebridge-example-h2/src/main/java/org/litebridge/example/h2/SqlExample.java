package org.litebridge.example.h2;

import org.litebridge.example.common.dto.Person;
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
        LOGGER.info("---======< SQL example >======---");

        // Retrieve a person's details using a lower-level SQL query
        litebridge.select("FIRST_NAME", "SURNAME", "AGE").from("LB", "PERSON")
                .where("AGE").gt(18)
                .and("AGE").lt(25)
                .stream()
                .forEach(record -> LOGGER.info("SQL result: Selected data for PERSON record: " + record));

        // Retrieve a person's details using a lower-level SQL query and map it to a DTO
        litebridge.select("FIRST_NAME", "SURNAME", "AGE").from("LB", "PERSON")
                .where("AGE").gt(18)
                .and("AGE").lt(25)
                .stream()
                .map(row -> litebridge.toDto(row, Person.class))
                .forEach(p -> LOGGER.info("SQL result: Mapped Person object: " + p));

        // Full example demonstrating the use of all SELECT clauses
        litebridge.select("FIRST_NAME", "SURNAME", "AGE").from("LB", "PERSON")
                .where("AGE").gte(18)
                .and("AGE").lt(25)
                .limit(10)
                .offset(1)
                .stream()
                .map(row -> litebridge.toDto(row, Person.class))
                .forEach(p -> LOGGER.info("SQL result: Mapped Person object: " + p));
    }

}
