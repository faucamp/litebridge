package org.litebridge.db.spi.query;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogicOperatorTest {

    @Test
    void enumValues() {
        assertEquals(3, LogicOperator.values().length);
        assertEquals(LogicOperator.AND, LogicOperator.valueOf("AND"));
        assertEquals(LogicOperator.OR, LogicOperator.valueOf("OR"));
        assertEquals(LogicOperator.NOOP, LogicOperator.valueOf("NOOP"));
    }
}
