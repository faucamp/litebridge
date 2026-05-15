package org.litebridgedb.example.spring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.litebridgedb.example.common.dto.Person;
import org.litebridgedb.example.spring.service.ExampleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/person")
public class PersonController {

    private final ExampleService exampleService;

    public PersonController(final ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    @PostMapping(value = "/random/{number}")
    @Operation(summary = "Create and return one or more random Person records")
    public List<Person> createRandomPersons(@PathVariable @Parameter(description = "Number of records to create") final int number) {
        if (number < 1) {
            throw new IllegalArgumentException("Number must be greater than 0");
        }

        return exampleService.createAndStoreRandomPersons(number);
    }

    @PostMapping
    @Operation(summary = "Create and return a new Person record")
    public Person createPerson(@Parameter(description = "Person to create; ID may be null") @RequestBody final Person person) {
        return exampleService.createPerson(person);
    }

    @GetMapping("/{personId}")
    @Operation(summary = "Get a Person record by ID")
    public Optional<Person> getPersonById(@RequestParam final Long personId) {
        return exampleService.getPersonById(personId);
    }

    @PutMapping
    @Operation(summary = "Update an existing Person record")
    public Person updatePerson(@Parameter(description = "Person to update; ID must be set") @RequestBody final Person person) {
        return exampleService.updatePerson(person);
    }

    @PostMapping("/{personId}/age/{age}")
    @Operation(summary = "Update the age of a person by ID")
    public Person updatePersonAge(@Parameter(description = "Person ID") @PathVariable final Long personId, @Parameter(description = "Age to set") @PathVariable final int age) {
        return exampleService.updatePersonAge(personId, age);
    }

    @GetMapping
    @Operation(summary = "Get all Person records")
    public List<Person> getAllPersons() {
        return exampleService.getAllPersons();
    }
}
