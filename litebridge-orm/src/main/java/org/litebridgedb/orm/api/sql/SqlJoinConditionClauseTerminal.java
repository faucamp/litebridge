package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.api.select.impl.AbstractJoinConditionClauseTerminal;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpecBuilder;
import org.litebridgedb.orm.api.select.model.GroupBySpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

public final class SqlJoinConditionClauseTerminal extends AbstractJoinConditionClauseTerminal<Row,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal,
        SqlGroupByClauseTerminal,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain,
        SqlSelectSpec,
        SqlJoinSpec>

        implements SqlJoinClauseTerminal {

    public SqlJoinConditionClauseTerminal(final SqlJoinSpec joinSpec, final SqlSelector delegate) {
        super(joinSpec, delegate);
    }

    @Override
    public SqlJoinConditionClause and(final String column) {
        final Column spiColumn = new Column(joinSpec.table(), column);
        return and(new SelectColumnSpec(spiColumn));
    }

    @Override
    public SqlJoinConditionClause and(final ExpressionSpec expression) {
        return joinImpl(LogicOperator.AND, expression);
    }

    @Override
    public SqlJoinConditionClause or(final String column) {
        final Column spiColumn = new Column(joinSpec.table(), column);
        return or(new SelectColumnSpec(spiColumn));
    }

    @Override
    public SqlJoinConditionClause or(final ExpressionSpec expression) {
        return joinImpl(LogicOperator.OR, expression);
    }

    @Override
    public SqlJoinClause join(final String table) {
        return new SqlJoinClause(selectSpec.newJoinSpec(table), (SqlSelector) delegate);
    }

    @Override
    public SqlWhereConditionClause where(final String column) {
        final Column spiColumn = new Column(selectSpec.getTable(), column);
        return where(new SelectColumnSpec(spiColumn));
    }

    @Override
    public SqlWhereConditionClause where(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.AND, expression);
    }

    @Override
    public SqlWhereConditionClauseTerminal where(final ConditionGroupSpecBuilder conditions) {
        final ConditionGroupSpec conditionGroupSpec = selectSpec.newWhereConditionGroup(LogicOperator.AND);
        conditions.accept(conditionGroupSpec);
        selectSpec.getWhereConditionGroups().add(conditionGroupSpec);
        return new SqlWhereConditionClauseTerminal((SqlSelector) delegate);
    }

    @Override
    public SqlGroupByClauseTerminal groupBy(final String... columns) {
        selectSpec.setGroupBy(new GroupBySpec(selectSpec.createSelectColumnSpecs(columns)));
        return new SqlGroupByClauseTerminal((SqlSelector) delegate);
    }

    @Override
    public SqlGroupByClauseTerminal groupBy(final ExpressionSpec... columns) {
        selectSpec.setGroupBy(new GroupBySpec(columns));
        return new SqlGroupByClauseTerminal((SqlSelector) delegate);
    }

    @Override
    public SqlOrderByClause orderBy(final String... columns) {
        return new SqlOrderByClause(selectSpec.newOrderBy(selectSpec.createSelectColumnSpecs(columns)), (SqlSelector) delegate);
    }

    @Override
    public SqlOrderByClause orderBy(final ExpressionSpec... columns) {
        return new SqlOrderByClause(selectSpec.newOrderBy(columns), (SqlSelector) delegate);
    }

    private SqlWhereConditionClause whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final ConditionGroupSpec conditionGroupSpec = selectSpec.newWhereConditionGroup(logicOperator);
        return new SqlWhereConditionClause(conditionGroupSpec.newCondition(expression), new SqlWhereConditionClauseTerminal((SqlSelector) delegate), delegate.litebridgeContext());
    }

    private SqlJoinConditionClause joinImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final ConditionGroupSpec conditionGroupSpec = selectSpec.newWhereConditionGroup(logicOperator);
        return new SqlJoinConditionClause(conditionGroupSpec.newCondition(expression), this, delegate.litebridgeContext());
    }
}
