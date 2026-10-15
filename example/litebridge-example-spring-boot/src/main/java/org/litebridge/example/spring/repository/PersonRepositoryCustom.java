package org.litebridge.example.spring.repository;

import org.litebridge.example.common.entity.Person;

import java.util.List;

public interface PersonRepositoryCustom {

    public List<Person> getPersonsInAgeRage(final int age);
}
