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
        SqlSelector currentDelegate = delegate;
        for (final ExpressionSpec expression : expressions) {
            currentDelegate = currentDelegate.withNode(new OrderByNode(currentDelegate.node(), expression, true));
        }
        return new SqlOrderByClauseChain(currentDelegate);
    }

    @Override
    public SqlOrderByClauseChain desc() {
        SqlSelector currentDelegate = delegate;
        for (final ExpressionSpec expression : expressions) {
            currentDelegate = currentDelegate.withNode(new OrderByNode(currentDelegate.node(), expression, false));
        }
        return new SqlOrderByClauseChain(currentDelegate);
    }
}
