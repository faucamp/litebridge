package org.litebridgedb.orm.expression;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.expression.ColumnExpressionImpl;

public class TestColumnExpression extends ColumnExpressionImpl {

    public TestColumnExpression(final Column column) {
        super(column);
    }

    @Override
    public String toSql(final Operation operation) {
        return column().name();
    }
}
