package org.litebridge.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.impl.expression.AbstractColumnExpression;
import org.litebridge.db.spi.impl.sql.LabelGenerator;

import java.util.Objects;

public class TestColumnExpression extends AbstractColumnExpression {

    private static final LabelGenerator labelGenerator = new LabelGenerator();

    public TestColumnExpression(final Column column) {
        super(column, null, null, labelGenerator);
    }

    @Override
    public String toSql(final Operation operation, final ClauseType context, final @Nullable DelegateExpression parent) {
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
