package org.litebridge.example.spring.repository;

import org.litebridge.example.common.dto.Person;
import org.litebridge.spring.repository.LitebridgeRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends LitebridgeRepository<Person, Long> {

    /**
     * Finds all persons with the specified name and surname.
     * <p>
     * This is an example of a query built using Spring's dynamic method query parsing.
     *
     * @param name    Person's first name
     * @param surname Person's surname
     * @return List of persons matching the specified name and surname
     */
    List<Person> findAllByNameAndSurname(String name, String surname);

    /**
     * Finds all persons with the specified name or surname.
     * <p>
     * This is an example of a query built using Spring's dynamic method query parsing.
     *
     * @param name First name/surname to search for.
     * @return List of persons matching the specified name and surname
     */
    List<Person> findAllByNameOrSurname(String name, String surname);

    /**
     * Count all persons with first names starting with the specified prefix.
     *
     * @param namePrefix Prefix of the first name to search for.
     * @return Number of persons matching the specified name prefix
     */
    int countAllByNameStartsWith(String namePrefix);
}
