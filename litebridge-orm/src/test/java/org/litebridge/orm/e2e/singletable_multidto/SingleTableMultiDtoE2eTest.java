package org.litebridge.orm.e2e.singletable_multidto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.litebridge.orm.e2e.AbstractE2eTest;
import org.litebridge.orm.e2e.singletable_multidto.dto.SingleTableNestedParent;
import org.litebridge.orm.e2e.singletable_multidto.mapping.DtoTableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.litebridge.orm.api.spec.TableSpec.t;

class SingleTableMultiDtoE2eTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleTableMultiDtoE2eTest.class);

    @Test
    @DisplayName("Nested DTOs mapped to a single table")
    void nestedDtos_singleTable() throws Exception {
        assumeTrue(litebridge.select().from("LB.PERSON").list().isEmpty());
        assumeTrue(litebridge.select().from("LB.ACCOUNT").list().isEmpty());

        // Register DTO-table mapping
        litebridge.register(SingleTableNestedParent.class, t("LB", "NESTED_DTO", DtoTableMap.SingeTableNestedDto));

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