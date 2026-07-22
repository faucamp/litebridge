package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.ast.GroupByNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.api.select.impl.AbstractJoinConditionClauseTerminal;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.GroupBySpec;
import org.litebridge.orm.api.sql.condition.SqlConditionClauseStart;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

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
    public SqlJoinConditionClauseTerminal and(final QueryConditionBuilder<Row> query) {
        return joinImpl(LogicOperator.AND, query);
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
    public SqlJoinConditionClauseTerminal or(final QueryConditionBuilder<Row> query) {
        return joinImpl(LogicOperator.OR, query);
    }

    @Override
    public SqlJoinClause join(final String table) {
        final org.litebridge.orm.api.select.ast.JoinNode joinNode = new org.litebridge.orm.api.select.ast.JoinNode(delegate.node(), "INNER", null, table);
        final SqlSelector newDelegate = (SqlSelector) delegate.withNode(joinNode);
        return new SqlJoinClause(selectSpec.newJoinSpec(table), newDelegate);
    }

    @Override
    public SqlWhereConditionClause where(final String column) {
        final Column spiColumn = new Column(selectSpec.getTable(), column);
        return where(new SelectColumnSpec(spiColumn));
    }

    @Override
    public SqlWhereConditionClause where(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.NOOP, expression);
    }

    @Override
    public SqlGroupByClauseTerminal groupBy(final String... columns) {
        return groupBy(selectSpec.createSelectColumnSpecs(columns).toArray(ExpressionSpec[]::new));
    }

    @Override
    public SqlGroupByClauseTerminal groupBy(final ExpressionSpec... columns) {
        final QueryNode groupByNode = new GroupByNode(delegate.node(), columns);
        return new SqlGroupByClauseTerminal((SqlSelector) delegate.withNode(groupByNode));
    }

    @Override
    public SqlOrderByClause orderBy(final String... columns) {
        return orderBy(selectSpec.createSelectColumnSpecs(columns).toArray(ExpressionSpec[]::new));
    }

    @Override
    public SqlOrderByClause orderBy(final ExpressionSpec... columns) {
        return new SqlOrderByClause(columns, (SqlSelector) delegate);
    }

    private SqlWhereConditionClause whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final ConditionSpec conditionSpec = selectSpec.currentWhereConditionGroupSpec().newCondition(logicOperator, expression);

        return new SqlWhereConditionClause(conditionSpec,
                delegate.litebridgeContext(),
                logicOperator,
                expression,
                delegate.node(),
                node -> new SqlWhereConditionClauseTerminal((SqlSelector) delegate.withNode(node)));
    }

    private SqlJoinConditionClause joinImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final ConditionSpec conditionSpec = joinSpec.currentConditionGroupSpec().newCondition(logicOperator, expression);

        final java.util.function.Function<org.litebridge.orm.api.select.ast.QueryNode, SqlJoinConditionClauseTerminal> recreator = n -> new SqlJoinConditionClauseTerminal(joinSpec, (SqlSelector) delegate.withNode(n));

        return new SqlJoinConditionClause(conditionSpec, delegate.litebridgeContext(), logicOperator, expression, delegate.node(), recreator);
    }

    private SqlJoinConditionClauseTerminal joinImpl(final LogicOperator logicOperator, final QueryConditionBuilder<Row> query) {
        final ConditionGroupSpec subgroup = joinSpec.pushConditionGroupSpec(logicOperator);
        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(subgroup, joinSpec.table(), delegate.litebridgeContext().fromClauseEngine());
        query.apply(conditionClauseStart);
        joinSpec.popConditionGroupSpec();
        return this;
    }
}
