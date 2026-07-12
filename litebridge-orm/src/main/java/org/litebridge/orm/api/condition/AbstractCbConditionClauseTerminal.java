package org.litebridge.orm.api.condition;

import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.dto.condition.CbDtoConditionClauseTerminal;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.sql.condition.CbSqlConditionClauseTerminal;
import org.litebridge.orm.engine.FromClauseEngine;
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

    /**
     * The condition group specification.
     */
    protected final ConditionGroupSpec conditionGroupSpec;

    /**
     * The engine used to process the FROM clause.
     */
    protected final FromClauseEngine fromClauseEngine;

    /**
     * Constructs a new {@code AbstractCbConditionClauseTerminal}.
     *
     * @param conditionGroupSpec The condition group specification.
     * @param fromClauseEngine   The FROM clause engine.
     */
    public AbstractCbConditionClauseTerminal(final ConditionGroupSpec conditionGroupSpec,
                                             final FromClauseEngine fromClauseEngine) {
        this.conditionGroupSpec = conditionGroupSpec;
        this.fromClauseEngine = fromClauseEngine;
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
     * Creates a new condition clause instance.
     *
     * @param conditionSpec The condition specification.
     * @return A new {@link AbstractCbConditionClause} instance.
     */
    protected abstract AbstractCbConditionClause<DTO> createCbConditionClause(final ConditionSpec conditionSpec);

    /**
     * Creates a new condition clause start instance for a subgroup.
     *
     * @param subgroup The condition group specification for the subgroup.
     * @return A new {@link AbstractConditionClauseStart} instance.
     */
    protected abstract AbstractConditionClauseStart<DTO> createConditionClauseStart(final ConditionGroupSpec subgroup);

    /**
     * Internal implementation of the WHERE clause for expressions.
     *
     * @param logicOperator The logical operator (AND/OR).
     * @param expression    The expression specification.
     * @return A new {@link AbstractCbConditionClause} instance.
     */
    protected final AbstractCbConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final ConditionSpec conditionSpec = conditionGroupSpec.newCondition(logicOperator, expression);
        return createCbConditionClause(conditionSpec);
    }

    private AbstractCbConditionClauseTerminal<DTO> whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
        final ConditionGroupSpec subgroup = conditionGroupSpec.newSubgroup(logicOperator).conditionGroupSpec();
        final AbstractConditionClauseStart<DTO> conditionClauseStart = createConditionClauseStart(subgroup);
        query.apply(conditionClauseStart);
        return this;
    }
}
