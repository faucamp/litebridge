package org.litebridge.orm.api.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.impl.AbstractJoinConditionClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;

public final class SqlJoinConditionClauseTerminal extends AbstractJoinConditionClauseTerminal<Row,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal,
        SqlGroupByClauseTerminal,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain>

        implements SqlJoinClauseTerminal {

    public SqlJoinConditionClauseTerminal(final JoinNode joinNode,
                                          final SelectEngineTerminal selectEngineTerminal,
                                          final LitebridgeContext litebridgeContext) {
        super(joinNode, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlJoinConditionClause and(final String column) {
//        final Column spiColumn = new Column(((SqlSelector) delegate).table(), column);
//        return and(new SelectColumnSpec(spiColumn));
        throw new UnsupportedOperationException("Not implemented yet");
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
//        final Column spiColumn = new Column(((SqlSelector) delegate).table(), column);
//        return or(new SelectColumnSpec(spiColumn));
        throw new UnsupportedOperationException("Not implemented yet");
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
//        return new SqlJoinClause((SqlSelector) delegate, node -> {
//            final JoinNode joinNode = new JoinNode(delegate.node(), "INNER", null, table);
//            joinNode.withCondition(node);
//            delegate.withNode(joinNode);
//            return new SqlJoinConditionClauseTerminal(joinNode, (SqlSelector) delegate);
//        });
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public SqlWhereConditionClause where(final String column) {
        return whereImpl(LogicOperator.NOOP, column, null);
    }

    @Override
    public SqlWhereConditionClause where(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.NOOP, null, expression);
    }

    @Override
    public SqlGroupByClauseTerminal groupBy(final String... columns) {
        return new SqlGroupByClauseTerminal(columns, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlGroupByClauseTerminal groupBy(final ExpressionSpec... expressions) {
        return new SqlGroupByClauseTerminal(expressions, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlOrderByClause orderBy(final String... columns) {
        return new SqlOrderByClause(columns, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlOrderByClause orderBy(final ExpressionSpec... expressions) {
        return new SqlOrderByClause(expressions, node, selectEngineTerminal, litebridgeContext);
    }

    private SqlWhereConditionClause whereImpl(final LogicOperator logicOperator, final @Nullable String column, final @Nullable ExpressionSpec expression) {
        if (node instanceof WhereNode whereNode) {
            return new SqlWhereConditionClause(litebridgeContext,
                    logicOperator,
                    column,
                    expression,
                    whereNode.condition(),
                    node -> new SqlWhereConditionClauseTerminal(whereNode.withCondition(node), selectEngineTerminal, litebridgeContext));
        }

        return new SqlWhereConditionClause(litebridgeContext,
                logicOperator,
                column,
                expression,
                null,
                conditionNode -> new SqlWhereConditionClauseTerminal(new WhereNode(this.node, conditionNode), selectEngineTerminal, litebridgeContext));
    }

    private SqlJoinConditionClause joinImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
//        final Function<QueryNode, SqlJoinConditionClauseTerminal> recreator = n -> {
//            joinNode.withCondition(n);
//            return this;
//        };
//        return new SqlJoinConditionClause(delegate.litebridgeContext(), logicOperator, expression, joinNode.condition(), recreator);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private SqlJoinConditionClauseTerminal joinImpl(final LogicOperator logicOperator, final QueryConditionBuilder<Row> query) {
//        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(((SqlSelector) delegate).table(), delegate.litebridgeContext().fromClauseEngine(), null);
//        final AbstractCbConditionClauseTerminal<Row> terminal = query.apply(conditionClauseStart);
//        final QueryNode conditionNode = terminal.node();
//
//        final ConditionGroupNode groupNode = new ConditionGroupNode(joinNode.condition(), logicOperator, conditionNode);
//        joinNode.withCondition(groupNode);
//
//        return this;
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
