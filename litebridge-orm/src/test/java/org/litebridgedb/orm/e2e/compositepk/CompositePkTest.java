package org.litebridgedb.orm.e2e.compositepk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.litebridgedb.orm.e2e.AbstractE2eTest;
import org.litebridgedb.orm.e2e.compositepk.dto.CompositePkFkTest;
import org.litebridgedb.orm.e2e.compositepk.dto.CompositePkLookup;
import org.litebridgedb.orm.e2e.compositepk.dto.CompositePkSimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompositePkTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompositePkTest.class);

    @TestTemplate
    @DisplayName("Composite PK with foreign key constraint")
    void compositePk_foreignKey() throws Exception {
        // Given
        litebridge.register(CompositePkLookup.class, rc -> rc.mapToTable("LB.COMP_PK_LOOKUP")
                .mapField("id").toColumn("LOOKUP_ID")
                .mapField("name").toColumn("LOOKUP_NAME"));

        litebridge.register(CompositePkFkTest.class, rc -> rc.mapToTable("LB.COMP_PK_FK_TEST")
                .mapField("lookup").toColumn("LOOKUP_ID").joinUsing()
                .mapField("testId").toColumn("TEST_ID")
                .mapField("description").toColumn("TEST_DESC"));

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

    @TestTemplate
    @DisplayName("Composite auto-incrementing PK")
    void compositePk_autoIncrement() throws Exception {
        // Given
        litebridge.register(CompositePkSimple.class, rc -> rc.mapToTable("LB.COMP_PK_SIMPLE")
                .mapField("pk1").toColumn("PK1").autoIncrement().usingSequence("LB.COMPOSITE_PK1_SEQ")
                .mapField("pk2").toColumn("PK2").autoIncrement().usingSequence("LB.COMPOSITE_PK2_SEQ")
                .mapField("description").toColumn("TEST_DESC"));

        final CompositePkSimple dto = new CompositePkSimple(null, null, "test");

        // When
        litebridge.save(dto);

        // Then
        final CompositePkSimple result = litebridge.select(CompositePkSimple.class)
                .where("pk1").eq(1L)
                .and("pk2").eq(2L)
                .oneOrThrow();
        assertEquals(1L, result.pk1());
        assertEquals(2L, result.pk2());
        assertEquals(dto.description(), result.description());
    }
}