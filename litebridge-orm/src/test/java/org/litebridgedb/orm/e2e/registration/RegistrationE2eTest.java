package org.litebridgedb.orm.e2e.registration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.litebridgedb.orm.e2e.AbstractE2eTest;
import org.litebridgedb.orm.e2e.setup.DbEnvDtoTableMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.litebridgedb.orm.e2e.registration.entities.RegistrationTestEntities.E0;
import static org.litebridgedb.orm.e2e.registration.entities.RegistrationTestEntities.E1;
import static org.litebridgedb.orm.e2e.registration.entities.RegistrationTestEntities.E2;
import static org.litebridgedb.orm.e2e.registration.entities.RegistrationTestEntities.E3;
import static org.litebridgedb.orm.e2e.registration.entities.RegistrationTestEntities.E4;
import static org.litebridgedb.orm.e2e.registration.entities.RegistrationTestEntities.E5;
import static org.litebridgedb.orm.e2e.registration.entities.RegistrationTestEntities.E6;
import static org.litebridgedb.orm.e2e.registration.entities.RegistrationTestEntities.E7;
import static org.litebridgedb.orm.e2e.registration.entities.RegistrationTestEntities.E8;
import static org.litebridgedb.orm.e2e.registration.entities.RegistrationTestEntities.E9;

public class RegistrationE2eTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationE2eTest.class);

    @TestTemplate
    @DisplayName("Select DTO and join fetch related DTOs")
    void register_entity_crossReferences(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Test entities specify the "LB" schema in the @Table annotation, so skip SQLite (no schemas) and Postgres (lowercase)
        assumeTrue(!dbEnv.getName().equals("SQLite") && !dbEnv.getName().equals("PostgreSQL"));

        litebridge.register(E0.class,
                E1.class,
                E2.class,
                E3.class,
                E4.class,
                E5.class,
                E6.class,
                E7.class,
                E8.class,
                E9.class);
    }
}