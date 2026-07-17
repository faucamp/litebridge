package org.litebridge.example.spring.db;

import jakarta.annotation.PostConstruct;
import org.litebridge.example.common.entity.Person;
import org.litebridge.orm.Litebridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class DatabaseFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseFacade.class);
    private final Litebridge litebridge;

    public DatabaseFacade(final Litebridge litebridge) {
        this.litebridge = litebridge;
    }

    @PostConstruct
    public void init() {
        LOGGER.info("Running some queries to test connection pool");

        for (int i = 0; i < 15; i++) {
            litebridge.select(Person.class).where("id").eq(i).one();
        }
    }

    @Transactional
    public void create(final Person person) {
        litebridge.insert(person);
    }

    @Transactional
    public void delete(final Long personId) {
        litebridge.delete(Person.class, p -> p.where("id").eq(personId));
    }

    @Transactional(readOnly = true)
    public Optional<Person> findPersonWithId(final Long personId) {
        return litebridge.select(Person.class).where("id").eq(personId).one();
    }

    @Transactional(readOnly = true)
    public List<Person> getAll() {
        return litebridge.select(Person.class).list();
    }

    @Transactional
    public List<Person> createAll(final List<Person> persons) {
        persons.forEach(litebridge::insert);
        return persons;
    }

    @Transactional
    public Person update(final Person person) {
        litebridge.update(person);
        return person;
    }
}
