package org.litebridgedb.orm.expression;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.ColumnExpressionFactory;

public class TestColumnExpressionFactory implements ColumnExpressionFactory {

    @Override
    public ColumnExpression create(final Column column, final Object... args) {
        return new TestColumnExpression(column);
    }
}
