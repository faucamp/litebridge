package org.litebridge.example.spring;

import org.junit.jupiter.api.Test;
import org.litebridge.example.common.dto.Person;
import org.litebridge.example.spring.controller.PersonController;
import org.litebridge.example.spring.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SpringBoot3PersonControllerTest {

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

    @Test
    void personController_getPersonWhereAgeBetweenExclusive() {
        // Given
        final Person person = new Person();
        person.setName("Daisy");
        person.setSurname("Miller");
        person.setAge(28);
        person.setEyeColour("Hazel");
        personRepository.save(person);

        // When
        final List<Person> found = personController.getPersonWhereAgeBetweenExclusive(25, 30);

        // Then
        assertThat(found).isNotEmpty();
    }

    @Test
    void personController_getPersonsInAgeRage() {
        // Given
        final Person person = new Person();
        person.setName("Daisy");
        person.setSurname("Miller");
        person.setAge(28);
        person.setEyeColour("Hazel");
        personRepository.save(person);

        // When
        final List<Person> found = personController.getPersonsInAgeRage(25);

        // Then
        assertThat(found).isNotEmpty();
    }

    @Test
    void personController_getAverageAge() {
        // Given
        final Person person = new Person();
        person.setName("Edward");
        person.setSurname("Norton");
        person.setAge(50);
        person.setEyeColour("Blue");
        personRepository.save(person);

        // When
        final double avgAge = personController.getAverageAge();

        // Then
        assertThat(avgAge).isGreaterThan(0.0);
    }

    @Test
    void updatePersonAge() {
        // Given
        final Person person = new Person();
        person.setName("Edward");
        person.setSurname("Norton");
        person.setAge(50);
        person.setEyeColour("Blue");
        personRepository.save(person);

        // When
        final Person result = personController.updatePersonAge(person.getId(), 55);

        // Then
        assertEquals(55, result.getAge());
    }

    @Test
    void updatePersonAgeNativeSql() {
        // Given
        final Person person = new Person();
        person.setName("Edward");
        person.setSurname("Norton");
        person.setAge(50);
        person.setEyeColour("Blue");
        personRepository.save(person);

        // When
        final int result = personController.updatePersonAgeNativeSql(person.getId(), 55);

        // Then
        assertEquals(1, result);
    }
}
