package org.litebridgedb.orm.e2e.annotation.entity;

import org.litebridgedb.orm.annotation.Column;
import org.litebridgedb.orm.annotation.OneToMany;
import org.litebridgedb.orm.annotation.Table;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@Table("LB.PERSON")
public class Person {

    @Column(value = "PERSON_ID", generateUsingSequence = "LB.PERSON_SEQ")
    private Long id;
    @Column("FIRST_NAME")
    private String name;
    @Column("SURNAME")
    private String surname;
    @Column("AGE")
    private int age;
    // Annotation on getter
    private String eyeColour;
    @OneToMany(mappedByField = "owner")
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

    @Column("EYE_COLOUR")
    public String getEyeColour() {
        return eyeColour;
    }

    public void setEyeColour(String eyeColour) {
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
                .add("accounts=" + (accounts != null ? accounts.stream().map(Account::getId).toList().toString() : null))
                .toString();
    }
}
