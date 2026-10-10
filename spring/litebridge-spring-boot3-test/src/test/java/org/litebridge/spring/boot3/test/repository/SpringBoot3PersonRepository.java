package org.litebridge.spring.boot3.test.repository;

import org.litebridge.spring.repository.LitebridgeRepository;

import java.util.List;

public interface SpringBoot3PersonRepository extends LitebridgeRepository<SpringBoot3Person, Long> {

    List<SpringBoot3Person> findByNameAndSurname(final String name, final String surname);

    long countByNameStartsWith(final String prefix);

    boolean existsByAge(final Integer age);
}
