package org.litebridge.orm.api.condition;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Abstract base class for the start of a condition clause in the fluent select API.
 *
 * @param <DTO> The type of the DTO being queried.
 */
public abstract class AbstractConditionClauseStart<DTO> {

    /**
     * The current query node.
     */
    protected final @Nullable QueryNode node;
    /**
     * The Litebridge context.
     */
    protected final LitebridgeContext litebridgeContext;

    /**
     * Constructs a new {@code AbstractConditionClauseStart}.
     *
     * @param node              the current query node
     * @param litebridgeContext the Litebridge context
     */
    public AbstractConditionClauseStart(@Nullable final QueryNode node, final LitebridgeContext litebridgeContext) {
        this.node = node;
        this.litebridgeContext = litebridgeContext;
    }

    /**
     * Starts a WHERE clause with a column name.
     *
     * @param column The column name.
     * @return A new {@link AbstractCbConditionClause} instance.
     */
    public abstract AbstractCbConditionClause<DTO> where(final String column);

    /**
     * Starts a WHERE clause with an expression.
     *
     * @param expression The expression specification.
     * @return A new {@link AbstractCbConditionClause} instance.
     */
    public abstract AbstractCbConditionClause<DTO> where(final ExpressionSpec expression);
}
