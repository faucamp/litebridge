package org.litebridge.db.oracle.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.oracle.OracleColumnIdentifierGenerator;
import org.litebridge.db.spi.math.MathOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OracleMathOperationGeneratorTest {

    private OracleMathOperationGenerator mathOperationGenerator;

    @BeforeEach
    void setUp() {
        final OracleColumnIdentifierGenerator columnIdentifierGenerator = new OracleColumnIdentifierGenerator();
        mathOperationGenerator = new OracleMathOperationGenerator(columnIdentifierGenerator);
    }

    @Test
    void createMathOperation_modOperator_returnsOracleModSyntax() {
        // Given
        final String column = "TOTAL";
        final MathOperator operator = MathOperator.MOD;

        // When
        final String result = mathOperationGenerator.createMathOperation(column, operator);

        // Then
        assertEquals("MOD(TOTAL, ?)", result);
    }

    @Test
    void createMathOperation_modOperator_quotesReservedWord() {
        // Given
        final String column = "ORDER";
        final MathOperator operator = MathOperator.MOD;

        // When
        final String result = mathOperationGenerator.createMathOperation(column, operator);

        // Then
        assertEquals("MOD(\"ORDER\", ?)", result);
    }

    @Test
    void createMathOperation_otherOperator_delegatesToSuper() {
        // Given
        final String column = "AMOUNT";
        final MathOperator operator = MathOperator.ADD;

        // When
        final String result = mathOperationGenerator.createMathOperation(column, operator);

        // Then
        assertEquals("AMOUNT + ?", result);
    }
}
