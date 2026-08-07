package org.litebridge.orm.api.condition;

import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.dto.condition.CbDtoConditionClause;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Abstract base class for the start of a condition clause in the fluent select API.
 *
 * @param <DTO> The type of the DTO being queried.
 */
public abstract class AbstractConditionClauseStart<DTO> {

    /**
     * The engine used to process the FROM clause.
     */
    protected final FromClauseEngine fromClauseEngine;

    /**
     * Constructs a new {@code AbstractConditionClauseStart}.
     *
     * @param fromClauseEngine   The FROM clause engine.
     */
    public AbstractConditionClauseStart(final FromClauseEngine fromClauseEngine) {
        this.fromClauseEngine = fromClauseEngine;
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
