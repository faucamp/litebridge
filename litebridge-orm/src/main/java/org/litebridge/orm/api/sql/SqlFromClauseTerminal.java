package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.ast.GroupByNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.api.select.impl.AbstractFromClauseTerminal;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.GroupBySpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

public final class SqlFromClauseTerminal extends AbstractFromClauseTerminal<Row,
        SqlJoinClause,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal,
        SqlGroupByClauseTerminal,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain,
        SqlSelectSpec>

        implements SqlJoinClauseTerminal {

    public SqlFromClauseTerminal(final SqlSelector delegate) {
        super(delegate);
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
}
