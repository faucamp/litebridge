package org.litebridge.db.spi.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MathOperatorTest {

    @Test
    void symbolsAndEnumValues() {
        // When & Then
        assertEquals("+", MathOperator.ADD.symbol());
        assertEquals("-", MathOperator.SUBTRACT.symbol());
        assertEquals("*", MathOperator.MULTIPLY.symbol());
        assertEquals("/", MathOperator.DIVIDE.symbol());
        assertEquals("%", MathOperator.MOD.symbol());

        assertEquals(5, MathOperator.values().length);
        assertEquals(MathOperator.ADD, MathOperator.valueOf("ADD"));
    }
}
