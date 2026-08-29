package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.HavingConditionClauseTerminal;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.api.select.impl.AbstractHavingClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.expression.ExpressionSpec;

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

    public SqlHavingConditionClauseTerminal(final QueryNode node, final SelectEngineTerminal selectEngineTerminal, final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlHavingConditionClause and(final String column) {
//        final Column spiColumn = new Column(((SqlSelector) delegate).table(), column);
//        return and(new SelectColumnSpec(spiColumn));
        throw new UnsupportedOperationException("Not implemented yet");
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
//        final Column spiColumn = new Column(((SqlSelector) delegate).table(), column);
//        return or(new SelectColumnSpec(spiColumn));
        throw new UnsupportedOperationException("Not implemented yet");
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
        return new SqlOrderByClause(columns, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlOrderByClause orderBy(final ExpressionSpec... expressions) {
        return new SqlOrderByClause(expressions, node, selectEngineTerminal, litebridgeContext);
    }

    private SqlHavingConditionClause havingImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
//        if (delegate.node() instanceof HavingNode havingNode) {
//            return new SqlHavingConditionClause(delegate.litebridgeContext(),
//                    logicOperator,
//                    expression,
//                    havingNode.condition(),
//                    node -> new SqlHavingConditionClauseTerminal((SqlSelector) delegate.withNode(havingNode.withCondition(node))));
//        }
//
//        return new SqlHavingConditionClause(delegate.litebridgeContext(),
//                logicOperator,
//                expression,
//                null,
//                node -> new SqlHavingConditionClauseTerminal((SqlSelector) delegate.withNode(new HavingNode(delegate.node(), node))));
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private SqlHavingConditionClauseTerminal havingImpl(final LogicOperator logicOperator, final QueryConditionBuilder<Row> query) {
//        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(((SqlSelector) delegate).table(), delegate.litebridgeContext().fromClauseEngine(), null);
//        final org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal<Row> terminal = query.apply(conditionClauseStart);
//        final QueryNode conditionNode = terminal.node();
//
//        if (delegate.node() instanceof HavingNode havingNode) {
//            final ConditionGroupNode groupNode = new ConditionGroupNode(havingNode.condition(), logicOperator, conditionNode);
//            havingNode.withCondition(groupNode);
//            return this;
//        }
//
//        final ConditionGroupNode groupNode = new ConditionGroupNode(null, logicOperator, conditionNode);
//        delegate.withNode(new HavingNode(delegate.node(), groupNode));
//
//        return this;
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
