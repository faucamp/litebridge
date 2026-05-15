package org.litebridgedb.db.spi.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MathOperationTest {

    @Test
    void testRecord() {
        MathOperation operation = new MathOperation(MathOperation.Operator.ADD, 10);
        assertEquals(MathOperation.Operator.ADD, operation.operator());
        assertEquals(10, operation.value());
    }

    @Test
    void testOperatorSymbols() {
        assertEquals("+", MathOperation.Operator.ADD.symbol());
        assertEquals("-", MathOperation.Operator.SUBTRACT.symbol());
        assertEquals("*", MathOperation.Operator.MULTIPLY.symbol());
        assertEquals("/", MathOperation.Operator.DIVIDE.symbol());
        assertEquals("%", MathOperation.Operator.MOD.symbol());
    }

    @Test
    void testOperatorValueOf() {
        assertEquals(MathOperation.Operator.ADD, MathOperation.Operator.valueOf("ADD"));
    }

    @Test
    void testOperatorValues() {
        assertEquals(5, MathOperation.Operator.values().length);
    }
}
