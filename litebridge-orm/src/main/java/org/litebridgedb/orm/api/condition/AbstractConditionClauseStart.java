package org.litebridgedb.orm.api.condition;

import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.expression.ExpressionSpec;

/**
 * Abstract base class for the start of a condition clause in the fluent select API.
 *
 * @param <DTO> The type of the DTO being queried.
 */
public abstract class AbstractConditionClauseStart<DTO> {

    /**
     * The condition group specification.
     */
    protected final ConditionGroupSpec conditionGroupSpec;

    /**
     * The engine used to process the FROM clause.
     */
    protected final FromClauseEngine fromClauseEngine;

    /**
     * Constructs a new {@code AbstractConditionClauseStart}.
     *
     * @param conditionGroupSpec The condition group specification.
     * @param fromClauseEngine   The FROM clause engine.
     */
    public AbstractConditionClauseStart(final ConditionGroupSpec conditionGroupSpec,
                                        final FromClauseEngine fromClauseEngine) {
        this.conditionGroupSpec = conditionGroupSpec;
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
    public final AbstractCbConditionClause<DTO> where(final ExpressionSpec expression) {
        final ConditionSpec conditionSpec = conditionGroupSpec.newCondition(LogicOperator.NOOP, expression);
        return createCbConditionClause(conditionSpec);
    }

    /**
     * Creates a new condition clause instance.
     *
     * @param conditionSpec The condition specification.
     * @return A new {@link AbstractCbConditionClause} instance.
     */
    protected abstract AbstractCbConditionClause<DTO> createCbConditionClause(final ConditionSpec conditionSpec);
}
