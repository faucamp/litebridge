package org.litebridgedb.orm.api.dto.delete;

import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.db.spi.update.UpdateResult;
import org.litebridgedb.orm.api.condition.QueryConditionBuilder;
import org.litebridgedb.orm.api.delete.DeleteTerminal;
import org.litebridgedb.orm.expression.ExpressionSpec;

public final class DtoDeleteWhereConditionClauseTerminalImpl<DTO>
        implements DtoDeleteWhereConditionClauseTerminal<DTO>,
        DeleteTerminal {

    private final DtoDeletor<DTO> delegate;

    public DtoDeleteWhereConditionClauseTerminalImpl(final DtoDeletor<DTO> delegate) {
        this.delegate = delegate;
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> and(final String field) {
        return delegate.whereImpl(LogicOperator.AND, field);
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> and(final ExpressionSpec expression) {
        return delegate.whereImpl(LogicOperator.AND, expression);
    }

    @Override
    public DtoDeleteWhereConditionClauseTerminal<DTO> and(final QueryConditionBuilder<DTO> query) {
        return delegate.whereImpl(LogicOperator.AND, query);
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> or(final String field) {
        return delegate.whereImpl(LogicOperator.OR, field);
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> or(final ExpressionSpec expression) {
        return delegate.whereImpl(LogicOperator.OR, expression);
    }

    @Override
    public DtoDeleteWhereConditionClauseTerminal<DTO> or(final QueryConditionBuilder<DTO> query) {
        return delegate.whereImpl(LogicOperator.OR, query);
    }

    @Override
    public UpdateResult execute() {
        return delegate.execute();
    }
}
