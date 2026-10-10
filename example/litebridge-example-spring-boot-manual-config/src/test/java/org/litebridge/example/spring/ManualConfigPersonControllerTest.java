package org.litebridge.example.spring;

import org.junit.jupiter.api.Test;
import org.litebridge.example.common.dto.Person;
import org.litebridge.example.spring.controller.PersonController;
import org.litebridge.example.spring.service.ExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ManualConfigPersonControllerTest {

    @Autowired
    private PersonController personController;

    @Autowired
    private ExampleService exampleService;

    @Test
    void personController_createsAndQueriesPersonsSuccessfully() {
        // Given
        final Person person = new Person();
        person.setName("Manual");
        person.setSurname("User");
        person.setAge(28);
        person.setEyeColour("Hazel");
        final Person created = personController.createPerson(person);

        // When
        final List<Person> found = personController.getPersonsByNameAndSurname("Manual", "User");
        final int count = personController.countPersonsNameStartingWith("Man");

        // Then
        assertThat(created.getId()).isNotNull();
        assertThat(found).isNotEmpty();
        assertThat(count).isGreaterThanOrEqualTo(1);
    }
}
