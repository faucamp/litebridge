package org.litebridge.db.spi.query;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperatorTest {

    @Test
    void enumValues() {
        assertEquals(13, Operator.values().length);
        assertEquals(Operator.EQ, Operator.valueOf("EQ"));
        assertEquals(Operator.NEQ, Operator.valueOf("NEQ"));
        assertEquals(Operator.GT, Operator.valueOf("GT"));
        assertEquals(Operator.GTE, Operator.valueOf("GTE"));
        assertEquals(Operator.LT, Operator.valueOf("LT"));
        assertEquals(Operator.LTE, Operator.valueOf("LTE"));
        assertEquals(Operator.LIKE, Operator.valueOf("LIKE"));
        assertEquals(Operator.IN, Operator.valueOf("IN"));
        assertEquals(Operator.NOT_IN, Operator.valueOf("NOT_IN"));
        assertEquals(Operator.IS_NULL, Operator.valueOf("IS_NULL"));
        assertEquals(Operator.IS_NOT_NULL, Operator.valueOf("IS_NOT_NULL"));
        assertEquals(Operator.USING, Operator.valueOf("USING"));
        assertEquals(Operator.EXISTS, Operator.valueOf("EXISTS"));
    }
}
