package org.litebridge.example.spring.repository;

import org.litebridge.example.common.dto.Person;
import org.litebridge.orm.Litebridge;

import java.util.List;

public class PersonRepositoryImpl implements PersonRepositoryCustom {

    private final Litebridge litebridge;

    public PersonRepositoryImpl(final Litebridge litebridge) {
        this.litebridge = litebridge;
    }

    @Override
    public List<Person> getPersonsInAgeRage(final int age) {
        return litebridge.select(Person.class)
                .where("age").gt(age - 5)
                .and("age").lt(age + 5)
                .list();
    }
}
