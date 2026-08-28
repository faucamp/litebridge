package org.litebridge.orm.api.condition;

import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.dto.condition.CbDtoConditionClauseTerminal;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.sql.condition.CbSqlConditionClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Abstract base class for terminal condition clauses in the fluent select API.
 *
 * @param <DTO> The type of the DTO being queried.
 */
public abstract sealed class AbstractCbConditionClauseTerminal<DTO>
        implements ConditionClauseTerminal<DTO, AbstractCbConditionClause<DTO>,
        AbstractCbConditionClauseTerminal<DTO>>

        permits CbDtoConditionClauseTerminal, CbSqlConditionClauseTerminal {

    protected final LitebridgeContext litebridgeContext;
    /**
     * The current query node in the AST.
     */
    protected QueryNode node;

    /**
     * Constructs a new {@code AbstractCbConditionClauseTerminal}.
     *
     * @param node The current query node.
     */
    public AbstractCbConditionClauseTerminal(final QueryNode node, final LitebridgeContext litebridgeContext) {
        this.node = node;
        this.litebridgeContext = litebridgeContext;
    }

    @Override
    public final AbstractCbConditionClause<DTO> and(final String field) {
        return whereImpl(LogicOperator.AND, field);
    }

    @Override
    public final AbstractCbConditionClause<DTO> and(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.AND, expression);
    }

    @Override
    public final AbstractCbConditionClauseTerminal<DTO> and(final QueryConditionBuilder<DTO> query) {
        return whereImpl(LogicOperator.AND, query);
    }

    @Override
    public final AbstractCbConditionClauseTerminal<DTO> or(final QueryConditionBuilder<DTO> query) {
        return whereImpl(LogicOperator.OR, query);
    }

    @Override
    public final AbstractCbConditionClause<DTO> or(final String field) {
        return whereImpl(LogicOperator.OR, field);
    }

    @Override
    public final AbstractCbConditionClause<DTO> or(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.OR, expression);
    }

    /**
     * Internal implementation of the WHERE clause for column names.
     *
     * @param logicOperator The logical operator (AND/OR).
     * @param column        The column name.
     * @return A new {@link AbstractCbConditionClause} instance.
     */
    protected abstract AbstractCbConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final String column);

    /**
     * Internal implementation of the WHERE clause for expressions.
     *
     * @param logicOperator The logical operator (AND/OR).
     * @param expression    The expression specification.
     * @return A new {@link AbstractCbConditionClause} instance.
     */
    protected abstract AbstractCbConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression);

    /**
     * Internal implementation of the WHERE clause for sub-conditions in queries.
     *
     * @param logicOperator The logical operator (AND/OR).
     * @param query         The sub-condition builder
     * @return A new {@link AbstractCbConditionClause} instance.
     */
    protected abstract AbstractCbConditionClauseTerminal<DTO> whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query);

    /**
     * Returns the current query node.
     *
     * @return the query node
     */
    public QueryNode node() {
        return node;
    }
}
