package org.litebridgedb.example.common;

import org.litebridgedb.example.common.dto.Account;
import org.litebridgedb.example.common.dto.Person;
import org.litebridgedb.orm.Litebridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

public class QueryExample extends AbstractExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryExample.class);

    public QueryExample(final Litebridge litebridge) {
        super(litebridge);
    }

    @Override
    public void run() {
        LOGGER.info("---======< Query example >======---");

        LOGGER.info("[EXAMPLE] Retrieve all persons and return a List");
        final List<Person> persons = litebridge.select(Person.class).list();
        LOGGER.info("All persons (list): {}", persons);

        LOGGER.info("[EXAMPLE] Retrieve a single person with criteria");
        final Person alice = litebridge.select(Person.class)
                .where("name").eq("Alice")
                .and("surname").eq("Smith")
                .oneOrThrow();
        LOGGER.info("Retrieved person (nullable): {}", alice);

        LOGGER.info("[EXAMPLE] Retrieve a single person with criteria using an Optional, and log it");
        litebridge.select(Person.class)
                .where("name").eq("Alice")
                .and("surname").eq("Smith")
                .one()
                .ifPresent(p -> LOGGER.info("Retrieved person (Optional): {}", p));

        LOGGER.info("[EXAMPLE] Retrieve oldest adult person with criteria using a Stream");
        litebridge.select(Person.class)
                .where("age").gte(18)
                .stream()
                .max(Comparator.comparing(Person::getAge))
                .ifPresent(p -> LOGGER.info("Oldest person: {}", p));

        LOGGER.info("[EXAMPLE] Retrieve and log Persons that have an eye colour set");
        litebridge.select(Person.class)
                .where("eyeColour").isNotNull()
                .stream()
                .forEach(p -> LOGGER.info("Person with eye colour (isNotNull): {}", p));

        LOGGER.info("[EXAMPLE] Retrieve and log Persons that do not have an eye colour set, using eq(null) instead of isNull()");
        litebridge.select(Person.class)
                .where("eyeColour").eq(null)
                .stream()
                .forEach(p -> LOGGER.info("Person without eye colour (eq): {}", p));

        LOGGER.info("[EXAMPLE] Retrieve and log the first person found, ordering by surname & age, ascending");
        litebridge.select(Person.class)
                .orderBy("surname", "age").asc()
                .first()
                .ifPresent(p -> LOGGER.info("First person record ordered by surname, age ASC: {}", p));

        LOGGER.info("[EXAMPLE] Retrieve and log the first person found, ordering by descending surname and ascending age");
        // - demonstrates how to use then() to chain multiple orderBy() calls
        litebridge.select(Person.class)
                .orderBy("surname").desc().then("age").asc()
                .first()
                .ifPresent(p -> LOGGER.info("First person record ordered by surname DESC, age ASC: {}", p));

        LOGGER.info("[EXAMPLE] Retrieve a single person record with offset");
        litebridge.select(Person.class)
                .orderBy("id").asc()
                .limit(1)
                .offset(1)
                .one()
                .ifPresent(p -> LOGGER.info("Person fetched with offset/limit: {}", p));

        LOGGER.info("[EXAMPLE] Select an account with a foreign key reference to a person");
        litebridge.select(Account.class)
                .where("owner").eq(alice.getId())
                .stream()
                .forEach(a -> LOGGER.info("Account with owner ID '{}': {}", alice.getId(), a));

        LOGGER.info("[EXAMPLE] Generate SQL without executing the query");
        final String sql = litebridge.select(Account.class)
                .where("owner").isNotNull()
                .toSql();
        LOGGER.info("Generated SQL: {}", sql);
    }
}
