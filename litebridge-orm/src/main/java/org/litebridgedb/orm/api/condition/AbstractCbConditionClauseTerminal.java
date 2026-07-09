package org.litebridgedb.orm.api.condition;

import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.api.dto.condition.CbDtoConditionClauseTerminal;
import org.litebridgedb.orm.api.select.ConditionClauseTerminal;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.api.sql.condition.CbSqlConditionClauseTerminal;
import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.expression.ExpressionSpec;

public abstract sealed class AbstractCbConditionClauseTerminal<DTO>
        implements ConditionClauseTerminal<DTO, AbstractCbConditionClause<DTO>,
        AbstractCbConditionClauseTerminal<DTO>>

        permits CbDtoConditionClauseTerminal, CbSqlConditionClauseTerminal {

    protected final ConditionGroupSpec conditionGroupSpec;
    protected final FromClauseEngine fromClauseEngine;

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
        return null;
    }

    protected abstract AbstractCbConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final String column);

    protected abstract AbstractCbConditionClause<DTO> createCbConditionClause(final ConditionSpec conditionSpec);

    protected abstract AbstractConditionClauseStart<DTO> createConditionClauseStart(final ConditionGroupSpec subgroup);

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
