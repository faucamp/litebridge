package org.litebridge.example.spring;

import org.junit.jupiter.api.Test;
import org.litebridge.example.common.dto.Person;
import org.litebridge.example.spring.controller.PersonController;
import org.litebridge.example.spring.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SpringBoot3PersonControllerTest {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PersonController personController;

    @Test
    void springBoot3PersonController_createsAndQueriesPersonsSuccessfully() {
        // Given
        final Person person = new Person();
        person.setName("Boot3");
        person.setSurname("User");
        person.setAge(22);
        person.setEyeColour("Gray");
        final Person created = personController.createPerson(person);

        // When
        final List<Person> found = personController.getPersonsByNameAndSurname("Boot3", "User");
        final int count = personController.countPersonsNameStartingWith("Boot");

        // Then
        assertThat(created.getId()).isNotNull();
        assertThat(found).isNotEmpty();
        assertThat(count).isGreaterThanOrEqualTo(1);
    }
}
