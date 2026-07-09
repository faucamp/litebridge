package org.litebridgedb.orm.api.sql.update;

import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.db.spi.update.UpdateResult;
import org.litebridgedb.orm.api.condition.QueryConditionBuilder;
import org.litebridgedb.orm.api.update.UpdateTerminal;
import org.litebridgedb.orm.api.update.model.UpdateSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;

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
