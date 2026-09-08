package org.litebridge.db.spi.impl.sql;

import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.math.MathOperator;

public class MathOperationGenerator {

    protected final ColumnIdentifierGenerator columnIdentifierGenerator;

    public MathOperationGenerator(final ColumnIdentifierGenerator columnIdentifierGenerator) {
        this.columnIdentifierGenerator = columnIdentifierGenerator;
    }

    /**
     * Creates a SQL representation of a math operation.
     *
     * @param column       the column
     * @param mathOperator the math operation
     * @return the SQL representation of the math operation
     */
    public String createMathOperation(final String column, final MathOperator mathOperator) {
        return "%s %s ?".formatted(columnIdentifierGenerator.quoteIdentifier(column), mathOperator.symbol());
    }
}
