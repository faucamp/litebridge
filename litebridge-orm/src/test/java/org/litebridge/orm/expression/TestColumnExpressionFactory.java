package org.litebridge.orm.expression;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.ColumnExpressionFactory;

public class TestColumnExpressionFactory implements ColumnExpressionFactory {

    @Override
    public ColumnExpression create(final Column column, final Object... args) {
        return new TestColumnExpression(column);
    }
}
