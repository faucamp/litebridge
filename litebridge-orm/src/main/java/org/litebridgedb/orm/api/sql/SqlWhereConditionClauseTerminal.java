package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.api.select.WhereConditionClauseTerminal;
import org.litebridgedb.orm.api.select.impl.AbstractWhereClauseTerminal;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.GroupBySpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

public final class SqlWhereConditionClauseTerminal
        extends AbstractWhereClauseTerminal<Row,
        SqlGroupByClauseTerminal,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain,
        SqlSelectSpec>

        implements WhereConditionClauseTerminal<Row,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal,
        SqlGroupByClauseTerminal,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    public SqlWhereConditionClauseTerminal(final SqlSelector delegate) {
        super(delegate);
    }

    @Override
    public SqlWhereConditionClause and(final String column) {
        final Column spiColumn = new Column(selectSpec.getTable(), column);
        return and(new SelectColumnSpec(spiColumn));
    }

    @Override
    public SqlWhereConditionClause and(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.AND, expression);
    }

    @Override
    public SqlWhereConditionClause or(final String column) {
        final Column spiColumn = new Column(selectSpec.getTable(), column);
        return or(new SelectColumnSpec(spiColumn));
    }

    @Override
    public SqlWhereConditionClause or(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.OR, expression);
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
}
