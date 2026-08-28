package org.litebridge.orm.api.condition;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Abstract base class for the start of a condition clause in the fluent select API.
 *
 * @param <DTO> The type of the DTO being queried.
 */
public abstract class AbstractConditionClauseStart<DTO> {

    protected final @Nullable QueryNode node;
    protected final LitebridgeContext litebridgeContext;

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
