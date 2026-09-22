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

    public Cast(final SelectExpression target, @Nullable final String alias, final int dataType, final LabelGenerator labelGenerator) {
        super(alias, null, labelGenerator);
        this.target = target;
        this.dataType = dataType;
    }

    @Override
    public SelectExpression target() {
        return target;
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        final String sql = "CAST(%s AS %s)".formatted(target.toSql(operation, clause), JDBCType.valueOf(dataType).getName());

        if (alias != null) {
            return sql + labelGenerator.createAliasAs(alias);
        }

        return sql;
    }
}
