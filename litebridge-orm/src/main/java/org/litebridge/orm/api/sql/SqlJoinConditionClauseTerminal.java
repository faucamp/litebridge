package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.ast.ConditionGroupNode;
import org.litebridge.orm.api.select.ast.GroupByNode;
import org.litebridge.orm.api.select.ast.JoinNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.api.select.impl.AbstractJoinConditionClauseTerminal;
import org.litebridge.orm.api.sql.condition.SqlConditionClauseStart;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import java.util.function.Function;

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

    public SqlJoinConditionClauseTerminal(final JoinNode joinNode, final SqlSelector delegate) {
        super(joinNode, delegate);
    }

    @Override
    public SqlJoinConditionClause and(final String column) {
        final Column spiColumn = new Column(((SqlSelector) delegate).table(), column);
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
        final Column spiColumn = new Column(((SqlSelector) delegate).table(), column);
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
        return new SqlJoinClause((SqlSelector) delegate, node -> {
            final JoinNode joinNode = new JoinNode(delegate.node(), "INNER", null, table);
            joinNode.withCondition(node);
            delegate.withNode(joinNode);
            return new SqlJoinConditionClauseTerminal(joinNode, (SqlSelector) delegate);
        });
    }

    @Override
    public SqlWhereConditionClause where(final String column) {
        final Column spiColumn = new Column(((SqlSelector) delegate).table(), column);
        return where(new SelectColumnSpec(spiColumn));
    }

    @Override
    public SqlWhereConditionClause where(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.NOOP, expression);
    }

    @Override
    public SqlGroupByClauseTerminal groupBy(final String... columns) {
        return groupBy(SqlSelectSpec.createSelectColumnSpecs(columns).toArray(ExpressionSpec[]::new));
    }

    @Override
    public SqlGroupByClauseTerminal groupBy(final ExpressionSpec... columns) {
        final QueryNode groupByNode = new GroupByNode(delegate.node(), columns);
        return new SqlGroupByClauseTerminal((SqlSelector) delegate.withNode(groupByNode));
    }

    @Override
    public SqlOrderByClause orderBy(final String... columns) {
        return orderBy(SqlSelectSpec.createSelectColumnSpecs(columns).toArray(ExpressionSpec[]::new));
    }

    @Override
    public SqlOrderByClause orderBy(final ExpressionSpec... columns) {
        return new SqlOrderByClause(columns, (SqlSelector) delegate);
    }

    private SqlWhereConditionClause whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        return new SqlWhereConditionClause(delegate.litebridgeContext(),
                logicOperator,
                expression,
                null,
                node -> new SqlWhereConditionClauseTerminal((SqlSelector) delegate.withNode(new WhereNode(delegate.node(), node))));
    }

    private SqlJoinConditionClause joinImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final Function<QueryNode, SqlJoinConditionClauseTerminal> recreator = n -> {
            joinNode.withCondition(n);
            return this;
        };
        return new SqlJoinConditionClause(delegate.litebridgeContext(), logicOperator, expression, joinNode.condition(), recreator);
    }

    private SqlJoinConditionClauseTerminal joinImpl(final LogicOperator logicOperator, final QueryConditionBuilder<Row> query) {
        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(((SqlSelector) delegate).table(), delegate.litebridgeContext().fromClauseEngine(), null);
        final AbstractCbConditionClauseTerminal<Row> terminal = query.apply(conditionClauseStart);
        final QueryNode conditionNode = terminal.node();

        final ConditionGroupNode groupNode = new ConditionGroupNode(joinNode.condition(), logicOperator, conditionNode);
        joinNode.withCondition(groupNode);

        return this;
    }
}
