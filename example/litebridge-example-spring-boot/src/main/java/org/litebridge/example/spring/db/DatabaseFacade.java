package org.litebridge.example.spring.db;

import org.litebridge.example.common.dto.Person;
import org.litebridge.example.common.mapping.CommonDtoRegistration;
import org.litebridge.orm.Litebridge;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class DatabaseFacade {

    private final Litebridge litebridge;

    public DatabaseFacade(final Litebridge litebridge) {
        CommonDtoRegistration.registerPersonAndAccount(litebridge);
        this.litebridge = litebridge;
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
