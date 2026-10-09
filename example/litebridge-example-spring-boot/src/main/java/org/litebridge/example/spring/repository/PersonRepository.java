package org.litebridge.example.spring.repository;

import org.litebridge.example.common.entity.Person;
import org.litebridge.spring.repository.LitebridgeRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends LitebridgeRepository<Person, Long> {
}
