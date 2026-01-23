package org.litebridge.orm.e2e.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class Person {

    private static final Logger LOGGER = LoggerFactory.getLogger(Person.class.getName());

    private Long id;
    private String name;
    private String surname;
    private int age;
    private String eyeColour;
    private List<Account> accounts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEyeColour() {
        LOGGER.debug("getEyeColour(): {}", eyeColour);
        return eyeColour;
    }

    public void setEyeColour(String eyeColour) {
        LOGGER.debug("setEyeColour(): {}", eyeColour);
        this.eyeColour = eyeColour;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(final List<Account> accounts) {
        this.accounts = accounts;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final Person person)) return false;
        return age == person.age && Objects.equals(id, person.id) && Objects.equals(name, person.name) && Objects.equals(surname, person.surname) && Objects.equals(eyeColour, person.eyeColour);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, surname, age, eyeColour);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Person.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name='" + name + "'")
                .add("surname='" + surname + "'")
                .add("age=" + age)
                .add("eyeColour='" + eyeColour + "'")
                .add("accounts=" + accounts)
                .toString();
    }
}
