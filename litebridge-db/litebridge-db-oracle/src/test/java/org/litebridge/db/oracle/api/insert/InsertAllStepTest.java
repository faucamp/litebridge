package org.litebridge.db.oracle.api.insert;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.InsertValuesNode;

import java.lang.reflect.Constructor;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class InsertAllStepTest {

    @Test
    void intoTable_sqlAndDtoModes_addsInsertValuesNodes() {
        // Given
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final InsertAllStep insertAllStep = new InsertAllStep(litebridgeContext);

        // When
        insertAllStep.intoTable("ACCOUNT", step -> step.into("ID", "NAME").values(1, "Alice"));
        insertAllStep.intoTable(String.class, step -> step.into("VALUE").values("Test"));

        // Then
        final List<InsertValuesNode> nodes = InsertAllStepInspector.insertValuesNodes(insertAllStep);
        assertEquals(2, nodes.size());
        assertEquals(nodes, insertAllStep.insertValuesNodes());
    }

    @Test
    void insertAllStepInspector_privateConstructor() throws Exception {
        final Constructor<InsertAllStepInspector> constructor = InsertAllStepInspector.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        final InsertAllStepInspector instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
