package org.litebridge.db.spi.impl.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.impl.sql.LabelGenerator;

import java.sql.JDBCType;

public class Cast extends AbstractAliasedExpression implements DelegateExpression {

    private final SelectExpression target;
    private final int dataType;
    private final @Nullable Integer size;

    public Cast(final SelectExpression target,
                @Nullable final String alias,
                final int dataType,
                final @Nullable Integer size,
                final LabelGenerator labelGenerator) {
        super(alias, null, labelGenerator);
        this.target = target;
        this.dataType = dataType;
        this.size = size;
    }

    @Override
    public SelectExpression target() {
        return target;
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        final StringBuilder sql = new StringBuilder("CAST(")
                .append(target.toSql(operation, clause, this))
                .append(" AS ")
                .append(JDBCType.valueOf(dataType).getName());

        if (size != null) {
            sql.append('(').append(size).append(')');
        }

        sql.append(')');

        if (alias != null) {
            sql.append(labelGenerator.createAliasAs(alias));
        }

        return sql.toString();
    }
}
