package org.litebridge.db.oracle.sql;

import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.sql.MathOperationGenerator;
import org.litebridge.db.spi.math.MathOperator;

public final class OracleMathOperationGenerator extends MathOperationGenerator {

    public OracleMathOperationGenerator(final ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(columnIdentifierGenerator);
    }

    @Override
    public String createMathOperation(final String column, final MathOperator mathOperator) {
        if (mathOperator == MathOperator.MOD) {
            return "MOD(%s, ?)".formatted(columnIdentifierGenerator.quoteIdentifier(column));
        } else {
            return super.createMathOperation(column, mathOperator);
        }
    }
}
