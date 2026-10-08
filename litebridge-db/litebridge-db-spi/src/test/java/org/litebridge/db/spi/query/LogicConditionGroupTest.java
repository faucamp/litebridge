package org.litebridge.db.spi.query;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class LogicConditionGroupTest {

    @Test
    void recordProperties() {
        // Given
        final ConditionGroup cg = new ConditionGroup(Collections.emptyList());
        final LogicConditionGroup lcg1 = new LogicConditionGroup(LogicOperator.AND, cg);
        final LogicConditionGroup lcg2 = new LogicConditionGroup(LogicOperator.AND, cg);
        final LogicConditionGroup lcgDiff = new LogicConditionGroup(LogicOperator.OR, cg);

        // When & Then
        assertEquals(LogicOperator.AND, lcg1.logicOperator());
        assertEquals(cg, lcg1.conditionGroup());
        assertEquals(lcg1, lcg2);
        assertEquals(lcg1.hashCode(), lcg2.hashCode());
        assertNotEquals(lcg1, lcgDiff);
    }
}
