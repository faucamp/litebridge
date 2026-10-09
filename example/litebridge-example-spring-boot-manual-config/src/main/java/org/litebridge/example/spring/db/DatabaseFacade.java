package org.litebridge.example.spring.db;

import org.litebridge.example.common.dto.Person;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.expression.Fn;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Example facade for database operations.
 */
@Component
public class DatabaseFacade {

    private final Litebridge litebridge;

    public DatabaseFacade(final Litebridge litebridge) {
        this.litebridge = litebridge;
    }

    public void create(final Person person) {
        litebridge.insert(person);
    }

    public void delete(final Long personId) {
        litebridge.delete(Person.class, p -> p.where("id").eq(personId));
    }

    public Optional<Person> findPersonWithId(final Long personId) {
        return litebridge.select(Person.class).where("id").eq(personId).one();
    }

    public List<Person> getAll() {
        return litebridge.select(Person.class).list();
    }

    public List<Person> getPersonsByNameAndSurname(final String name, final String surname) {
        return litebridge.select(Person.class)
                .where("name").eq(name)
                .and("surname").eq(surname)
                .list();
    }

    public List<Person> getPersonsByNameOrSurname(final String name) {
        return litebridge.select(Person.class)
                .where("name").eq(name)
                .or("surname").eq(name)
                .list();
    }

    public int countPersonsNameStartingWith(final String namePrefix) {
        return litebridge.select(Fn.convert(Fn.count(), int.class)).from(Person.class)
                .where("name").like('%' + namePrefix)
                .oneOrThrow();
    }

    @Transactional
    public List<Person> createAll(final List<Person> persons) {
        litebridge.saveAll(persons);
        return persons;
    }

    public Person update(final Person person) {
        litebridge.update(person);
        return person;
    }
}
