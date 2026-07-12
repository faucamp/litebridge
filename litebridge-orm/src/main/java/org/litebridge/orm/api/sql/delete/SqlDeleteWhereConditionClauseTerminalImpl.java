package org.litebridge.orm.api.sql.delete;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.delete.DeleteTerminal;
import org.litebridge.orm.expression.ExpressionSpec;

public final class SqlDeleteWhereConditionClauseTerminalImpl

        implements
        SqlDeleteWhereConditionClauseTerminal,
        DeleteTerminal {

    private final SqlDeletor delegate;

    public SqlDeleteWhereConditionClauseTerminalImpl(final SqlDeletor delegate) {
        this.delegate = delegate;
    }

    @Override
    public UpdateResult execute() {
        return delegate.execute();
    }

    @Override
    public SqlDeleteWhereConditionClause and(final String column) {
        return delegate.whereImpl(LogicOperator.AND, column);
    }

    @Override
    public SqlDeleteWhereConditionClause and(final ExpressionSpec expression) {
        return delegate.whereImpl(LogicOperator.AND, expression);
    }

    @Override
    public SqlDeleteWhereConditionClauseTerminal and(final QueryConditionBuilder<Row> query) {
        return delegate.whereImpl(LogicOperator.AND, query);
    }

    @Override
    public SqlDeleteWhereConditionClause or(final String column) {
        return delegate.whereImpl(LogicOperator.OR, column);
    }

    @Override
    public SqlDeleteWhereConditionClause or(final ExpressionSpec expression) {
        return delegate.whereImpl(LogicOperator.OR, expression);
    }

    @Override
    public SqlDeleteWhereConditionClauseTerminal or(final QueryConditionBuilder<Row> query) {
        return delegate.whereImpl(LogicOperator.OR, query);
    }
}
