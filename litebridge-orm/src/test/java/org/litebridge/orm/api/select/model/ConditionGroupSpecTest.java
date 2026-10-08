package org.litebridge.orm.api.select.model;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConditionGroupSpecTest {

    @Test
    void newCondition() {
        // Given
        final ConditionGroupSpec conditionGroupSpec = new ConditionGroupSpec();

        // When
        final ConditionSpec result = conditionGroupSpec.newCondition(LogicOperator.NOOP, "testcol", null, Operator.EQ, "abc");

        // Then
        assertEquals("testcol", result.lhsColumn());
        assertEquals("abc", result.value());
        assertEquals(1, conditionGroupSpec.conditions().size());
        assertEquals(0, conditionGroupSpec.subgroups().size());

        // When
        final ConditionSpec result2 = conditionGroupSpec.newCondition(LogicOperator.AND, "testcol2", null, Operator.EQ, "def");

        // Then
        assertEquals("testcol2", result2.lhsColumn());
        assertEquals("def", result2.value());
        assertEquals(2, conditionGroupSpec.conditions().size());
        assertEquals(0, conditionGroupSpec.subgroups().size());
    }

    @Test
    void newSubgroup() {
        // Given
        final ConditionGroupSpec conditionGroupSpec = new ConditionGroupSpec();

        // When
        final LogicConditionGroupSpec result = conditionGroupSpec.newSubgroup(LogicOperator.OR);

        // Then
        assertEquals(LogicOperator.OR, result.logicOperator());
        assertEquals(0, conditionGroupSpec.conditions().size());
        assertEquals(1, conditionGroupSpec.subgroups().size());

        // When
        final LogicConditionGroupSpec result2 = conditionGroupSpec.newSubgroup(LogicOperator.AND);

        // Then
        assertEquals(LogicOperator.AND, result2.logicOperator());
        assertEquals(0, conditionGroupSpec.conditions().size());
        assertEquals(2, conditionGroupSpec.subgroups().size());
    }
}