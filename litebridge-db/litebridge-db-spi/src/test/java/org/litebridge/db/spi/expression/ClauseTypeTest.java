package org.litebridge.db.spi.expression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClauseTypeTest {

    @Test
    void enumValues() {
        assertEquals(7, ClauseType.values().length);
        assertEquals(ClauseType.SELECT, ClauseType.valueOf("SELECT"));
        assertEquals(ClauseType.JOIN, ClauseType.valueOf("JOIN"));
        assertEquals(ClauseType.WHERE, ClauseType.valueOf("WHERE"));
        assertEquals(ClauseType.GROUP_BY, ClauseType.valueOf("GROUP_BY"));
        assertEquals(ClauseType.HAVING, ClauseType.valueOf("HAVING"));
        assertEquals(ClauseType.ORDER_BY, ClauseType.valueOf("ORDER_BY"));
        assertEquals(ClauseType.VALUES, ClauseType.valueOf("VALUES"));
    }
}
