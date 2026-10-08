package org.litebridge.db.spi.impl.sql;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.math.MathOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MathOperationGeneratorTest {

    private final LabelGenerator labelGenerator = new LabelGenerator();
    private final MathOperationGenerator mathOperationGenerator = new MathOperationGenerator(labelGenerator);

    @Test
    void createMathOperation_allOperators() {
        assertEquals("count + ?",
                mathOperationGenerator.createMathOperation("count", MathOperator.ADD));

        assertEquals("balance - ?",
                mathOperationGenerator.createMathOperation("balance", MathOperator.SUBTRACT));

        assertEquals("price * ?",
                mathOperationGenerator.createMathOperation("price", MathOperator.MULTIPLY));

        assertEquals("ratio / ?",
                mathOperationGenerator.createMathOperation("ratio", MathOperator.DIVIDE));

        assertEquals("value % ?",
                mathOperationGenerator.createMathOperation("value", MathOperator.MOD));
    }
}
