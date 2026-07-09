package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.api.select.impl.AbstractGroupByClauseTerminal;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;

public class SqlGroupByClauseTerminal extends AbstractGroupByClauseTerminal<Row,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain,
        SqlSelectSpec> {

    public SqlGroupByClauseTerminal(final SqlSelector delegate) {
        super(delegate);
    }

    @Override
    public SqlHavingConditionClause having(final ExpressionSpec expression) {
        return havingImpl(LogicOperator.NOOP, expression);
    }

    @Override
    public SqlOrderByClause orderBy(final String... columns) {
        return new SqlOrderByClause(selectSpec.newOrderBy(selectSpec.createSelectColumnSpecs(columns)), (SqlSelector) delegate);
    }

    @Override
    public SqlOrderByClause orderBy(final ExpressionSpec... columns) {
        return new SqlOrderByClause(selectSpec.newOrderBy(columns), (SqlSelector) delegate);
    }

    private SqlHavingConditionClause havingImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final ConditionSpec conditionSpec = selectSpec.currentHavingConditionGroupSpec().newCondition(logicOperator, expression);
        return new SqlHavingConditionClause(conditionSpec, new SqlHavingConditionClauseTerminal((SqlSelector) delegate), delegate.litebridgeContext());
    }
}
