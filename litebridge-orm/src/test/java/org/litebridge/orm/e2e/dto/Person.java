package org.litebridge.orm.e2e.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Person {

    private static final Logger LOGGER = LoggerFactory.getLogger(Person.class.getName());

    private Long id;
    private String name;
    private String surname;
    private int age;
    private String eyeColour;

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
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", age=" + age +
                ", eyeColour='" + eyeColour + '\'' +
                '}';
    }
}
