package org.litebridgedb.orm.e2e.singletable_multidto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.litebridgedb.orm.e2e.AbstractE2eTest;
import org.litebridgedb.orm.e2e.setup.DbEnvDtoTableMapper;
import org.litebridgedb.orm.e2e.singletable_multidto.dto.SingleTableNestedParent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class SingleTableMultiDtoE2eTest extends AbstractE2eTest {

    @TestTemplate
    @DisplayName("Nested DTOs mapped to a single table")
    void nestedDtos_singleTable(final DbEnvDtoTableMapper tableMapper) throws Exception {
        assumeTrue(litebridge.select().from(tableMapper.qualifyName("PERSON")).list().isEmpty());
        assumeTrue(litebridge.select().from(tableMapper.qualifyName("ACCOUNT")).list().isEmpty());

        // Register DTO-table mapping
        litebridge.register(SingleTableNestedParent.class, rc -> rc.mapToTable(tableMapper.qualifyName("NESTED_DTO"))
                .with(spec -> spec.mapField("parentValue1").toColumn(tableMapper.transformColumnName("PARENT_VALUE1")))
                .with(spec -> spec.mapField("nestedChild.childValue1").toColumn(tableMapper.transformColumnName("CHILD_VALUE1")))
                .with(spec -> spec.mapField("nestedChild.grandChild.grandChildValue1").toColumn(tableMapper.transformColumnName("GRANDCHILD_VALUE1"))));

        // Create DTOs and enable change tracking
        final SingleTableNestedParent singleTableNestedParent = litebridge.track(new SingleTableNestedParent());
        singleTableNestedParent.setParentValue1("testParentValue1");
        singleTableNestedParent.setNestedChild(new SingleTableNestedParent.NestedChild());
        singleTableNestedParent.getNestedChild().setChildValue1("testChildValue1");
        singleTableNestedParent.getNestedChild().setGrandChild(new SingleTableNestedParent.NestedChild.NestedGrandChild());
        singleTableNestedParent.getNestedChild().getGrandChild().setGrandChildValue1("testGrandChildValue1");

        // Save DTO and load it back
        litebridge.save(singleTableNestedParent);
        final SingleTableNestedParent result = litebridge.select(SingleTableNestedParent.class)
                .oneOrThrow();

        // Then
        assertTrue(result != singleTableNestedParent);
        assertEquals("testParentValue1", result.getParentValue1());
        assertNotNull(result.getNestedChild());
        assertEquals("testChildValue1", result.getNestedChild().getChildValue1());
        assertNotNull(result.getNestedChild().getGrandChild());
        assertEquals("testGrandChildValue1", result.getNestedChild().getGrandChild().getGrandChildValue1());
    }
}