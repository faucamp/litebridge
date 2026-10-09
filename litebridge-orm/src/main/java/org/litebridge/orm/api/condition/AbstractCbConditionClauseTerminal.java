package org.litebridge.orm.api.condition;

import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Abstract base class for terminal condition clauses in the fluent select API.
 *
 * @param <DTO> The type of the DTO being queried.
 */
public abstract sealed class AbstractCbConditionClauseTerminal<DTO,
        CC extends AbstractCbConditionClause<DTO, CC, SELF, QCB>,
        SELF extends AbstractCbConditionClauseTerminal<DTO, CC, SELF, QCB>,
        QCB extends QueryConditionBuilder<DTO, ?, CC, SELF, QCB>>

        implements ConditionClauseTerminal<DTO, CC, SELF, QCB>

        permits CbDtoConditionClauseTerminal, CbSqlConditionClauseTerminal {

    /**
     * The Litebridge context.
     */
    protected final LitebridgeContext litebridgeContext;
    /**
     * The current query node in the AST.
     */
    protected final QueryNode node;

    /**
     * Constructs a new {@code AbstractCbConditionClauseTerminal}.
     *
     * @param node              The current query node.
     * @param litebridgeContext The Litebridge context.
     */
    public AbstractCbConditionClauseTerminal(final QueryNode node, final LitebridgeContext litebridgeContext) {
        this.node = node;
        this.litebridgeContext = litebridgeContext;
    }

    @Override
    public final CC and(final String field) {
        return whereImpl(LogicOperator.AND, field);
    }

    @Override
    public final CC and(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.AND, expression);
    }

    @Override
    public final SELF and(final QCB query) {
        return whereImpl(LogicOperator.AND, query);
    }

    @Override
    public final SELF or(final QCB query) {
        return whereImpl(LogicOperator.OR, query);
    }

    @Override
    public final CC or(final String field) {
        return whereImpl(LogicOperator.OR, field);
    }

    @Override
    public final CC or(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.OR, expression);
    }

    /**
     * Internal implementation of the WHERE clause for column names.
     *
     * @param logicOperator The logical operator (AND/OR).
     * @param column        The column name.
     * @return A new {@link AbstractCbConditionClause} instance.
     */
    protected abstract CC whereImpl(final LogicOperator logicOperator, final String column);

    /**
     * Internal implementation of the WHERE clause for expressions.
     *
     * @param logicOperator The logical operator (AND/OR).
     * @param expression    The expression specification.
     * @return A new {@link AbstractCbConditionClause} instance.
     */
    protected abstract CC whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression);

    /**
     * Internal implementation of the WHERE clause for sub-conditions in queries.
     *
     * @param logicOperator The logical operator (AND/OR).
     * @param query         The sub-condition builder
     * @return A new {@link AbstractCbConditionClause} instance.
     */
    protected abstract SELF whereImpl(final LogicOperator logicOperator, final QCB query);

    /**
     * Returns the current query node.
     *
     * @return the query node
     */
    QueryNode node() {
        return node;
    }
}
