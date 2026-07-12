package org.litebridge.orm.api.sql.update;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.update.UpdateTerminal;
import org.litebridge.orm.api.update.model.UpdateSpec;
import org.litebridge.orm.expression.ExpressionSpec;

public final class SqlUpdateWhereConditionClauseTerminalImpl

        implements
        SqlUpdateWhereConditionClauseTerminal,
        UpdateTerminal {

    private final SqlUpdater delegate;

    public SqlUpdateWhereConditionClauseTerminalImpl(final SqlUpdater delegate) {
        this.delegate = delegate;
    }

    @Override
    public UpdateSpec updateSpec() {
        return delegate.updateSpec();
    }

    @Override
    public UpdateResult execute() {
        return delegate.execute();
    }

    @Override
    public SqlUpdateWhereConditionClause and(final String column) {
        return delegate.whereImpl(LogicOperator.AND, column);
    }

    @Override
    public SqlUpdateWhereConditionClause and(final ExpressionSpec expression) {
        return delegate.whereImpl(LogicOperator.AND, expression);
    }

    @Override
    public SqlUpdateWhereConditionClauseTerminal and(final QueryConditionBuilder<Row> query) {
        return delegate.whereImpl(LogicOperator.AND, query);
    }

    @Override
    public SqlUpdateWhereConditionClause or(final String column) {
        return delegate.whereImpl(LogicOperator.OR, column);
    }

    @Override
    public SqlUpdateWhereConditionClause or(final ExpressionSpec expression) {
        return delegate.whereImpl(LogicOperator.OR, expression);
    }

    @Override
    public SqlUpdateWhereConditionClauseTerminal or(final QueryConditionBuilder<Row> query) {
        return delegate.whereImpl(LogicOperator.OR, query);
    }
}
