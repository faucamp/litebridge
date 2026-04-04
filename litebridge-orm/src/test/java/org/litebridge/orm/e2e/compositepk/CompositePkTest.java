package org.litebridge.orm.e2e.compositepk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.litebridge.orm.e2e.AbstractE2eTest;
import org.litebridge.orm.e2e.basic.dto.Account;
import org.litebridge.orm.e2e.compositepk.dto.CompositePkFkTest;
import org.litebridge.orm.e2e.compositepk.dto.CompositePkLookup;
import org.litebridge.orm.e2e.compositepk.mapping.DtoTableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.litebridge.orm.api.spec.TableSpec.t;

class CompositePkTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompositePkTest.class);

    @Test
    @DisplayName("Select DTO and join fetch related DTOs")
    void nestedDtos_fetchRelatedDtos() throws Exception {
        // Given
        litebridge.register(CompositePkLookup.class, t("LB.COMP_PK_LOOKUP", DtoTableMap.CompositePkLookup));
        litebridge.register(CompositePkFkTest.class, t("LB.COMP_PK_FK_TEST", DtoTableMap.CompositePkFkTest));

        final CompositePkLookup lookup = new CompositePkLookup(123L, "Category 1");
        final CompositePkFkTest test1 = new CompositePkFkTest(lookup, 1L, "Test 1");
        final CompositePkFkTest test2 = new CompositePkFkTest(lookup, 2L, "Test 1");

        // When
        litebridge.save(lookup);
        litebridge.save(test1);
        litebridge.save(test2);

        // Then
        final CompositePkFkTest test1Result = litebridge.select(CompositePkFkTest.class)
                .join(CompositePkLookup.class).on("lookup")
                .where("lookup.id").eq(123L)
                .and("testId").eq(1L)
                .oneOrThrow();
        assertEquals(test1, test1Result);
    }
}