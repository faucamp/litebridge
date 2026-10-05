package org.litebridge.db.spi.impl.sql;

import org.litebridge.db.spi.math.MathOperator;

public class MathOperationGenerator {

    protected final LabelGenerator labelGenerator;

    public MathOperationGenerator(final LabelGenerator labelGenerator) {
        this.labelGenerator = labelGenerator;
    }

    /**
     * Creates a SQL representation of a math operation.
     *
     * @param column       the column
     * @param mathOperator the math operation
     * @return the SQL representation of the math operation
     */
    public String createMathOperation(final String column, final MathOperator mathOperator) {
        return "%s %s ?".formatted(labelGenerator.quoteIdentifier(column), mathOperator.symbol());
    }
}
