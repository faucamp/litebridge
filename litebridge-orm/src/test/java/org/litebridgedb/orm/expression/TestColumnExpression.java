package org.litebridgedb.orm.expression;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.expression.ColumnExpressionImpl;

import java.util.Objects;

public class TestColumnExpression extends ColumnExpressionImpl {

    public TestColumnExpression(final Column column) {
        super(column);
    }

    @Override
    public String toSql(final Operation operation) {
        return column().name();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof TestColumnExpression testColumnExpression
                && Objects.equals(this.column, testColumnExpression.column);
    }

    @Override
    public int hashCode() {
        return column.hashCode();
    }
}
