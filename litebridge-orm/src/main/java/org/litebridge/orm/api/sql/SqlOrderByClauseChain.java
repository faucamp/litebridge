package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.api.select.impl.OrderByClauseTerminalImpl;
import org.litebridge.orm.expression.ExpressionSpec;

public final class SqlOrderByClauseChain
        extends OrderByClauseTerminalImpl<Row, SqlSelectSpec>
        implements OrderByClauseChain<Row, SqlOrderByClause, SqlOrderByClauseChain> {

    public SqlOrderByClauseChain(final SqlSelector delegate) {
        super(delegate);
    }

    @Override
    public SqlOrderByClause then(final String... columns) {
        return new SqlOrderByClause(SqlSelectSpec.createSelectColumnSpecs(columns).toArray(ExpressionSpec[]::new), (SqlSelector) delegate);
    }

    @Override
    public SqlOrderByClause then(final ExpressionSpec... columns) {
        return new SqlOrderByClause(columns, (SqlSelector) delegate);
    }
}
