package org.litebridgedb.orm.api.condition;

import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.expression.ExpressionSpec;

public abstract class AbstractConditionClauseStart<DTO> {

    protected final ConditionGroupSpec conditionGroupSpec;
    protected final FromClauseEngine fromClauseEngine;

    public AbstractConditionClauseStart(final ConditionGroupSpec conditionGroupSpec,
                                        final FromClauseEngine fromClauseEngine) {
        this.conditionGroupSpec = conditionGroupSpec;
        this.fromClauseEngine = fromClauseEngine;
    }

    public abstract AbstractCbConditionClause<DTO> where(final String column);

    public final AbstractCbConditionClause<DTO> where(final ExpressionSpec expression) {
        final ConditionSpec conditionSpec = conditionGroupSpec.newCondition(LogicOperator.NOOP, expression);
        return createCbConditionClause(conditionSpec);
    }

    protected abstract AbstractCbConditionClause<DTO> createCbConditionClause(final ConditionSpec conditionSpec);
}
