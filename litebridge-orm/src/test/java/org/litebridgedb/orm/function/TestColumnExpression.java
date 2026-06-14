package org.litebridgedb.orm.function;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.query.ColumnExpression;

public class TestColumnExpression extends ColumnExpression {

    public TestColumnExpression(final Column column) {
        super(column);
    }

    @Override
    public String toSql(final Operation operation) {
        return column().name();
    }
}
