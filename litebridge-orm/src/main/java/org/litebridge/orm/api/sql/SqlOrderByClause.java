package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.ast.OrderByNode;
import org.litebridge.orm.expression.ExpressionSpec;

public final class SqlOrderByClause implements OrderByClause<Row, SqlOrderByClause, SqlOrderByClauseChain> {

    private final ExpressionSpec[] expressions;
    private final SqlSelector delegate;

    public SqlOrderByClause(final ExpressionSpec[] expressions, final SqlSelector delegate) {
        this.expressions = expressions;
        this.delegate = delegate;
    }

    @Override
    public SqlOrderByClauseChain asc() {
        for (final ExpressionSpec expression : expressions) {
            delegate.withNode(new OrderByNode(delegate.node(), expression, true));
        }

        return new SqlOrderByClauseChain(delegate);
    }

    @Override
    public SqlOrderByClauseChain desc() {
        for (final ExpressionSpec expression : expressions) {
            delegate.withNode(new OrderByNode(delegate.node(), expression, false));
        }
        return new SqlOrderByClauseChain(delegate);
    }
}
