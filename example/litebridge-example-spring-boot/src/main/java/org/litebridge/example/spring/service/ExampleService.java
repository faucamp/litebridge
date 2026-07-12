package org.litebridge.example.spring.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.litebridge.example.common.dto.Person;
import org.litebridge.example.spring.db.DatabaseFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class ExampleService {

    private final DatabaseFacade databaseFacade;

    public ExampleService(final DatabaseFacade databaseFacade) {
        this.databaseFacade = databaseFacade;
    }

    public List<Person> createAndStoreRandomPersons(final int number) {
        final List<Person> persons = new ArrayList<>();

        for (int i = 0; i < number; i++) {
            final Person person = new Person();
            person.setName(RandomStringUtils.insecure().nextAlphabetic(8));
            person.setSurname(RandomStringUtils.insecure().nextAlphabetic(8));
            person.setAge(new Random().nextInt(18, 65));
            persons.add(person);
        }

        return databaseFacade.createAll(persons);
    }

    public Person createPerson(final Person person) {
        databaseFacade.create(person);
        return person;
    }

    public List<Person> getAllPersons() {
        return databaseFacade.getAll();
    }

    public Optional<Person> getPersonById(final Long personId) {
        return databaseFacade.findPersonWithId(personId);
    }

    public Person updatePerson(final Person person) {
        if (person.getId() == null) {
            throw new IllegalArgumentException("Person ID cannot be null");
        }

        return databaseFacade.update(person);
    }

    @Transactional
    public Person updatePersonAge(final Long personId, final int age) {
        if (age < 0 || age > 120) {
            throw new IllegalArgumentException("Age must be between 0 and 120");
        }

        final Person person = databaseFacade.findPersonWithId(personId).orElseThrow();
        person.setAge(age);
        return databaseFacade.update(person);
    }
}
