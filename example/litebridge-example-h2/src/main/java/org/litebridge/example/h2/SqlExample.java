package org.litebridge.example.h2;

import org.litebridge.example.common.dto.Person;
import org.litebridge.orm.Litebridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;

import static org.litebridge.db.spi.Column.c;

public class SqlExample extends AbstractExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlExample.class);

    public SqlExample(final Litebridge litebridge) {
        super(litebridge);
    }

    @Override
    public void run() {
        LOGGER.info("---======< SQL example >======---");

        LOGGER.info("[EXAMPLE] Retrieve all persons");
        final List<LinkedHashMap<String, Object>> personRows =
                litebridge.select().from("LB", "PERSON")
                        .orderBy("PERSON_ID").asc()
                        .list();

        personRows.forEach(row -> LOGGER.info("Row data for PERSON record: {}", row));

        LOGGER.info("[EXAMPLE] Retrieve persons using a WHERE clause");
        litebridge.select("FIRST_NAME", "SURNAME", "AGE").from("LB", "PERSON")
                .where("AGE").gt(18)
                .and("AGE").lt(25)
                .stream()
                .forEach(record -> LOGGER.info("SQL result: Selected data for PERSON record: " + record));

        LOGGER.info("[EXAMPLE] Retrieve persons using SQL and map results to a DTO");
        litebridge.select("FIRST_NAME", "SURNAME", "AGE").from("LB", "PERSON")
                .where("AGE").gt(18)
                .and("AGE").lt(25)
                .orderBy("PERSON_ID").asc()
                .stream()
                .map(row -> litebridge.toDto(row, Person.class))
                .forEach(p -> LOGGER.info("SQL result: Mapped Person object: " + p));

        LOGGER.info("[EXAMPLE] Using joins");
        litebridge.select(
                        c("PERSON", "FIRST_NAME"),
                        c("PERSON", "SURNAME"),
                        c("PERSON", "AGE"),
                        c("ACCOUNT", "ACCOUNT_ID"),
                        c("ACCOUNT", "ACCOUNT_NAME"))
                .from("LB", "PERSON")
                .join("LB", "ACCOUNT").using("PERSON_ID")
                .stream()
                .forEach(p -> LOGGER.info("Joined result: " + p));
    }

}
