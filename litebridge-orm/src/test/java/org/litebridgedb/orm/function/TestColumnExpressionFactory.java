package org.litebridgedb.orm.function;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.query.ColumnExpression;
import org.litebridgedb.db.spi.query.ColumnExpressionFactory;

public class TestColumnExpressionFactory implements ColumnExpressionFactory {

    @Override
    public ColumnExpression create(final Column column) {
        return new TestColumnExpression(column);
    }
}
