package org.litebridge.example.spring.repository;

import org.litebridge.db.spi.Row;
import org.litebridge.example.common.dto.Person;
import org.litebridge.spring.annotation.Query;
import org.litebridge.spring.repository.LitebridgeRepository;
import org.springframework.data.repository.query.Param;
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

    /**
     * Existence check, filtered by age.
     *
     * @param age Age to filter on
     * @return True if a person with the specified age exists, false otherwise
     */
    boolean existsByAge(int age);

    /**
     * Retrieves all persons with age between minAge and maxAge.
     *
     * @param minAge Minimum age to search for.
     * @param maxAge Maximum age to search for.
     * @return List of persons matching the specified age range
     */
    List<Person> findAllByAgeBetween(int minAge, int maxAge);

    /**
     * Native query via annotation using named parameters out of order
     */
    @Query("SELECT * FROM LB.PERSON p WHERE p.AGE < :maxAge AND p.AGE > :minAge")
    List<Person> getPersonWhereAgeBetweenExclusive(@Param("minAge") int minAge, @Param("maxAge") int maxAge);

    /**
     * Native query using positional parameters, returning raw Row results
     */
    @Query("SELECT * FROM LB.PERSON p WHERE p.AGE > ? AND p.AGE < ?")
    List<Row> getPersonRowsWhereAgeBetweenExclusive(int minAge, int maxAge);

    /**
     * Native query with mapped return type
     */
    @Query("SELECT AVG(AGE) FROM LB.PERSON p")
    double getAverageAgeNativeQuery();
}
