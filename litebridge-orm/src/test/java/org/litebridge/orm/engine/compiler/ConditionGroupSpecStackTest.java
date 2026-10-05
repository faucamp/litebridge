package org.litebridge.orm.engine.compiler;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionGroupSpecStackTest {

    @Test
    void newRootInstanceCreatesFreshConditionGroupSpec() {
        // Given
        final ConditionGroupSpecStack stack = new ConditionGroupSpecStack();

        // When
        final ConditionGroupSpec rootSpec = stack.current();

        // Then
        assertNotNull(rootSpec);
        assertTrue(rootSpec.conditions().isEmpty());
        assertTrue(rootSpec.subgroups().isEmpty());
        assertFalse(stack.isEmpty());
        assertEquals(1, stack.all().size());
        assertSame(rootSpec, stack.all().getFirst());
    }

    @Test
    void newSubInstanceCreatesSubgroupAndLinksToParent() {
        // Given
        final ConditionGroupSpecStack stack = new ConditionGroupSpecStack();
        final ConditionGroupSpec rootSpec = stack.current();

        // When
        final ConditionGroupSpec subSpec = stack.push(LogicOperator.AND);

        // Then
        assertNotNull(subSpec);
        assertSame(subSpec, stack.current());
        assertEquals(2, stack.all().size());
        assertEquals(1, rootSpec.subgroups().size());
        assertEquals(LogicOperator.AND, rootSpec.subgroups().getFirst().logicOperator());
        assertSame(subSpec, rootSpec.subgroups().getFirst().conditionGroupSpec());

        // When popping back
        stack.pop();

        // Then
        assertSame(rootSpec, stack.current());
    }

    @Test
    void multipleNestedSubgroups() {
        // Given
        final ConditionGroupSpecStack stack = new ConditionGroupSpecStack();
        final ConditionGroupSpec root = stack.current();

        // When
        final ConditionGroupSpec sub1 = stack.push(LogicOperator.OR);
        final ConditionGroupSpec sub2 = stack.push(LogicOperator.AND);

        // Then
        assertSame(sub2, stack.current());
        assertEquals(1, sub1.subgroups().size());
        assertEquals(LogicOperator.AND, sub1.subgroups().getFirst().logicOperator());
        assertSame(sub2, sub1.subgroups().getFirst().conditionGroupSpec());

        // When popping
        stack.pop();
        assertSame(sub1, stack.current());

        stack.pop();
        assertSame(root, stack.current());
    }
}
