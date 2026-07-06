package org.litebridgedb.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.expression.ClauseType;
import org.litebridgedb.db.spi.expression.DelegateExpression;
import org.litebridgedb.db.spi.expression.SelectReference;

import java.util.Objects;

public class TestSelectReference extends SelectReference {

    public TestSelectReference(final Column column) {
        super(column);
    }

    @Override
    public String toSql(final Operation operation, final ClauseType context, final @Nullable DelegateExpression parent) {
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
