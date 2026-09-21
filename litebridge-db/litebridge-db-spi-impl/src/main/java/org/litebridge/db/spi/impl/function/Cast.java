package org.litebridge.db.spi.impl.function;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.impl.DatabaseProviderContext;

import java.sql.JDBCType;

public class Cast implements DelegateExpression {

    private final SelectExpression target;
    private final @Nullable String alias;
    private final int dataType;

    public Cast(final SelectExpression target, @Nullable final String alias, final int dataType) {
        this.target = target;
        this.alias = alias;
        this.dataType = dataType;
    }

    @Override
    public SelectExpression target() {
        return target;
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent, final Object providerContext) {
        final String sql = "CAST(%s AS %s)".formatted(target.toSql(operation, clause, this), JDBCType.valueOf(dataType).getName());

        if (alias != null) {
            return sql + " AS " + alias;
        }

        return sql;
    }
}
