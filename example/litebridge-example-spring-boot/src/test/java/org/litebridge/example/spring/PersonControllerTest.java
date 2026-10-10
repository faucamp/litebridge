package org.litebridge.example.spring;

import org.junit.jupiter.api.Test;
import org.litebridge.example.common.entity.Person;
import org.litebridge.example.spring.controller.PersonController;
import org.litebridge.example.spring.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PersonControllerTest {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PersonController personController;

    @Test
    void existsByAge_evaluatesExistenceCorrectly() {
        // Given
        final Person person = new Person();
        person.setName("Alice");
        person.setSurname("Smith");
        person.setAge(25);
        person.setEyeColour("Blue");
        personRepository.save(person);

        // When
        final boolean exists25 = personRepository.existsByAge(25);
        final boolean exists99 = personRepository.existsByAge(99);

        // Then
        assertThat(exists25).isTrue();
        assertThat(exists99).isFalse();
    }

    @Test
    void personController_existByAgeEndpointReturnsExpectedResult() {
        // Given
        final Person person = new Person();
        person.setName("Bob");
        person.setSurname("Jones");
        person.setAge(35);
        person.setEyeColour("Brown");
        personController.createPerson(person);

        // When
        final boolean exists35 = personController.existByAge(35);
        final boolean exists100 = personController.existByAge(100);

        // Then
        assertThat(exists35).isTrue();
        assertThat(exists100).isFalse();
    }

    @Test
    void personRepository_executesQueriesAndCount() {
        // Given
        final Person person = new Person();
        person.setName("Charlie");
        person.setSurname("Brown");
        person.setAge(40);
        person.setEyeColour("Green");
        personRepository.save(person);

        // When
        final List<Person> found = personRepository.findAllByNameAndSurname("Charlie", "Brown");
        final int count = personRepository.countAllByNameStartsWith("Char");

        // Then
        assertThat(found).isNotEmpty();
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    void personController_getPersonsByAgeBetween() {
        // Given
        final Person person = new Person();
        person.setName("Charlie");
        person.setSurname("Brown");
        person.setAge(40);
        person.setEyeColour("Green");
        personRepository.save(person);

        // When
        final List<Person> found = personController.getPersonsByAgeBetween(30, 50);

        // Then
        assertThat(found).isNotEmpty();
    }
}
