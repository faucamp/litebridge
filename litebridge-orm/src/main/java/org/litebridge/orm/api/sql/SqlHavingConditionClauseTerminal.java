package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.HavingConditionClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractHavingClauseTerminal;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.sql.condition.SqlConditionClauseStart;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

public final class SqlHavingConditionClauseTerminal
        extends AbstractHavingClauseTerminal<Row,
        SqlOrderByClause,
        SqlOrderByClauseChain,
        SqlSelectSpec>

        implements HavingConditionClauseTerminal<Row,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    public SqlHavingConditionClauseTerminal(final SqlSelector delegate) {
        super(delegate);
    }

    @Override
    public SqlHavingConditionClause and(final String column) {
        final Column spiColumn = new Column(selectSpec.getTable(), column);
        return and(new SelectColumnSpec(spiColumn));
    }

    @Override
    public SqlHavingConditionClause and(final ExpressionSpec expression) {
        return havingImpl(LogicOperator.AND, expression);
    }

    @Override
    public SqlHavingConditionClauseTerminal and(final QueryConditionBuilder<Row> query) {
        return havingImpl(LogicOperator.AND, query);
    }

    @Override
    public SqlHavingConditionClause or(final String column) {
        final Column spiColumn = new Column(selectSpec.getTable(), column);
        return or(new SelectColumnSpec(spiColumn));
    }

    @Override
    public SqlHavingConditionClause or(final ExpressionSpec expression) {
        return havingImpl(LogicOperator.OR, expression);
    }

    @Override
    public SqlHavingConditionClauseTerminal or(final QueryConditionBuilder<Row> query) {
        return havingImpl(LogicOperator.OR, query);
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

    private SqlHavingConditionClauseTerminal havingImpl(final LogicOperator logicOperator, final QueryConditionBuilder<Row> query) {
        final ConditionGroupSpec subgroup = selectSpec.pushHavingConditionGroup(logicOperator);
        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(subgroup, selectSpec.getTable(), delegate.litebridgeContext().fromClauseEngine());
        query.apply(conditionClauseStart);
        selectSpec.popHavingConditionGroup();
        return this;
    }
}
