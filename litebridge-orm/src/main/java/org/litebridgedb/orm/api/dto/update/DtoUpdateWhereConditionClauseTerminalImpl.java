package org.litebridgedb.orm.api.dto.update;

import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.db.spi.update.UpdateResult;
import org.litebridgedb.orm.api.condition.QueryConditionBuilder;
import org.litebridgedb.orm.api.update.UpdateTerminal;
import org.litebridgedb.orm.api.update.model.UpdateSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;

public final class DtoUpdateWhereConditionClauseTerminalImpl<DTO>
        implements DtoUpdateWhereConditionClauseTerminal<DTO>,
        UpdateTerminal {

    private final DtoUpdater<DTO> delegate;

    public DtoUpdateWhereConditionClauseTerminalImpl(final DtoUpdater<DTO> delegate) {
        this.delegate = delegate;
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> and(final String field) {
        return delegate.whereImpl(LogicOperator.AND, field);
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> and(final ExpressionSpec expression) {
        return delegate.whereImpl(LogicOperator.AND, expression);
    }

    @Override
    public DtoUpdateWhereConditionClauseTerminal<DTO> and(final QueryConditionBuilder<DTO> query) {
        return delegate.whereImpl(LogicOperator.AND, query);
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> or(final String field) {
        return delegate.whereImpl(LogicOperator.OR, field);
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> or(final ExpressionSpec expression) {
        return delegate.whereImpl(LogicOperator.OR, expression);
    }

    @Override
    public DtoUpdateWhereConditionClauseTerminal<DTO> or(final QueryConditionBuilder<DTO> query) {
        return delegate.whereImpl(LogicOperator.OR, query);
    }

    @Override
    public UpdateSpec updateSpec() {
        return delegate.updateSpec();
    }

    @Override
    public UpdateResult execute() {
        return delegate.execute();
    }
}
