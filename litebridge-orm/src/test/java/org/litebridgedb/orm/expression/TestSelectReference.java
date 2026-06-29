package org.litebridgedb.orm.expression;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.expression.SelectReference;

import java.util.Objects;

public class TestSelectReference extends SelectReference {

    public TestSelectReference(final Column column) {
        super(column);
    }

    @Override
    public String toSql(final Operation operation) {
        return column().name();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof TestSelectReference testColumnExpression
                && Objects.equals(this.column, testColumnExpression.column);
    }

    @Override
    public int hashCode() {
        return column.hashCode();
    }
}
