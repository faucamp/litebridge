package org.litebridge.orm.api.dto.update;

import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.update.UpdateTerminal;
import org.litebridge.orm.api.update.model.UpdateSpec;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Implementation of the terminal interface for DTO update where condition clauses.
 *
 * @param <DTO> the DTO type
 */
public final class DtoUpdateWhereConditionClauseTerminalImpl<DTO>
        implements DtoUpdateWhereConditionClauseTerminal<DTO>,
        UpdateTerminal {

    private final DtoUpdater<DTO> delegate;

    /**
     * Creates a new DtoUpdateWhereConditionClauseTerminalImpl.
     *
     * @param delegate the DTO updater to delegate to
     */
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
