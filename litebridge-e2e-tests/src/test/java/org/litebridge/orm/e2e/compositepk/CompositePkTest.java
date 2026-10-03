package org.litebridge.orm.e2e.compositepk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.e2e.AbstractE2eTest;
import org.litebridge.orm.e2e.compositepk.dto.CompositePkFkTest;
import org.litebridge.orm.e2e.compositepk.dto.CompositePkLookup;
import org.litebridge.orm.e2e.compositepk.dto.CompositePkSimple;
import org.litebridge.orm.e2e.compositepk.dto.CompositePkSimpleM2M;
import org.litebridge.orm.e2e.setup.DbEnvDtoTableMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class CompositePkTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompositePkTest.class);

    @TestTemplate
    @DisplayName("Composite PK with foreign key constraint")
    void compositePk_foreignKey(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Given
        litebridge.register(CompositePkLookup.class, rc -> rc.mapToTable(tableMapper.qualifyName("COMP_PK_LOOKUP"))
                .with(spec -> spec.mapField("id").toColumn(tableMapper.transformColumnName("LOOKUP_ID")))
                .with(spec -> spec.mapField("name").toColumn(tableMapper.transformColumnName("LOOKUP_NAME"))));

        litebridge.register(CompositePkFkTest.class, rc -> rc.mapToTable(tableMapper.qualifyName("COMP_PK_FK_TEST"))
                .with(spec -> spec.mapField("lookup").toColumn(tableMapper.transformColumnName("LOOKUP_ID")).joinUsing())
                .with(spec -> spec.mapField("testId").toColumn(tableMapper.transformColumnName("TEST_ID")))
                .with(spec -> spec.mapField("description").toColumn(tableMapper.transformColumnName("TEST_DESC"))));

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

        // Retrieve without join - NULL non-joined fields (default behaviour)
        final CompositePkFkTest testResult2 = litebridge.select(CompositePkFkTest.class)
                .where("lookup.id").eq(123L)
                .and("testId").eq(1L)
                .oneOrThrow();
        assertEquals(test1.testId(), testResult2.testId());
        assertEquals(test1.description(), testResult2.description());
        assertNull(testResult2.lookup());

        // Retrieve without join - partially construct related DTOs since no JOIN is specified
        final CompositePkFkTest testResult3 = litebridge.select(CompositePkFkTest.class, RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN)
                .where("lookup.id").eq(123L)
                .and("testId").eq(1L)
                .oneOrThrow();
        assertEquals(test1.testId(), testResult3.testId());
        assertEquals(test1.description(), testResult3.description());
        assertNotEquals(test1.lookup(), testResult3.lookup());
        assertEquals(test1.lookup().id(), testResult3.lookup().id());
        assertNull(testResult3.lookup().name());
    }

    @TestTemplate
    @DisplayName("Composite auto-incrementing PK")
    void compositePk_autoIncrement(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Not applicable for SQLite
        assumeTrue(!dbEnv.getName().equals("SQLite"), "SQLite does not support multiple auto-incrementing expressions");

        // Given
        litebridge.register(CompositePkSimple.class, rc -> rc
                .mapToTable(tableMapper.qualifyName("COMP_PK_SIMPLE"))
                .with(spec -> spec.mapField("pk1").toColumn(tableMapper.transformColumnName("PK1")).generateUsingSequence("LB.COMPOSITE_PK1_SEQ"))
                .with(spec -> spec.mapField("pk2").toColumn(tableMapper.transformColumnName("PK2")).generateUsingSequence("LB.COMPOSITE_PK2_SEQ"))
                .with(spec -> spec.mapField("description").toColumn(tableMapper.transformColumnName("TEST_DESC"))));

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

    @TestTemplate
    @DisplayName("Select withId() composite PK")
    void select_withId_compositePk(final DbEnvDtoTableMapper tableMapper) {
        // Given
        litebridge.register(CompositePkLookup.class, rc -> rc.mapToTable(tableMapper.qualifyName("COMP_PK_LOOKUP"))
                .with(spec -> spec.mapField("id").toColumn(tableMapper.transformColumnName("LOOKUP_ID")))
                .with(spec -> spec.mapField("name").toColumn(tableMapper.transformColumnName("LOOKUP_NAME"))));

        litebridge.register(CompositePkFkTest.class, rc -> rc.mapToTable(tableMapper.qualifyName("COMP_PK_FK_TEST"))
                .with(spec -> spec.mapField("lookup").toColumn(tableMapper.transformColumnName("LOOKUP_ID")).joinUsing())
                .with(spec -> spec.mapField("testId").toColumn(tableMapper.transformColumnName("TEST_ID")))
                .with(spec -> spec.mapField("description").toColumn(tableMapper.transformColumnName("TEST_DESC"))));

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

        // Retrieve without join - NULL non-joined fields (default behaviour)
        final CompositePkFkTest result2 = litebridge.select(CompositePkFkTest.class)
                .withIdOrThrow(List.of(123, 1L));

        assertNull(result2.lookup());
        assertEquals(test1.testId(), result2.testId());
        assertEquals(test1.description(), result2.description());
    }


    @TestTemplate
    @DisplayName("Many-to-many join with composite PKs")
    void compositePk_manyToManyJoin(final DbEnvDtoTableMapper tableMapper) throws Exception {
        final String compPkSimpleTable = tableMapper.qualifyName("COMP_PK_SIMPLE");
        final String compJoinTable = tableMapper.qualifyName("COMP_JOIN_TABLE");

        // Given
        litebridge.register(CompositePkSimpleM2M.class, rc -> rc
                .mapToTable(compPkSimpleTable)
                .with(spec -> spec.mapField("pk1").toColumn(tableMapper.transformColumnName("PK1")))
                .with(spec -> spec.mapField("pk2").toColumn(tableMapper.transformColumnName("PK2")))
                .with(spec -> spec.mapField("description").toColumn(tableMapper.transformColumnName("TEST_DESC")))
                .with(spec -> spec.mapField("others").manyToMany(m -> m
                        .joinTable(compJoinTable)
                        .joinColumns(
                                tableMapper.transformColumnName("LEFT_PK1"),
                                tableMapper.transformColumnName("LEFT_PK2"))
                        .inverseJoinColumns(
                                tableMapper.transformColumnName("RIGHT_PK1"),
                                tableMapper.transformColumnName("RIGHT_PK2")))));

        final CompositePkSimpleM2M dto1 = new CompositePkSimpleM2M();
        dto1.setPk1(1L);
        dto1.setPk2(2L);
        dto1.setDescription("test");

        final CompositePkSimpleM2M dto2 = new CompositePkSimpleM2M();
        dto2.setPk1(100L);
        dto2.setPk2(200L);
        dto1.setDescription("other");

        dto1.setOthers(List.of(dto2));
        dto2.setOthers(List.of(dto1));

        // When
        litebridge.save(dto1);

        // Then
        final CompositePkSimpleM2M result = litebridge.select(CompositePkSimpleM2M.class)
                .where("pk1").eq(1L)
                .and("pk2").eq(2L)
                .oneOrThrow();
        assertEquals(1L, result.getPk1());
        assertEquals(2L, result.getPk2());
        assertEquals(dto1.getDescription(), result.getDescription());
        assertNotNull(result.getOthers());
        assertEquals(1, result.getOthers().size());
        final CompositePkSimpleM2M resultOther = result.getOthers().getFirst();
        assertEquals(100L, resultOther.getPk1());
        assertEquals(200L, resultOther.getPk2());
        assertNotNull(resultOther.getOthers());
        assertEquals(1, resultOther.getOthers().size());
        assertEquals(result, resultOther.getOthers().getFirst());
    }
}