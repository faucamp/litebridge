package org.litebridge.orm.api.select.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.HavingConditionClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractHavingClauseTerminal;
import org.litebridge.orm.api.condition.SqlConditionClauseStart;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.HavingNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Terminal clause for SQL HAVING conditions.
 */
public final class SqlHavingConditionClauseTerminal
        extends AbstractHavingClauseTerminal<Row,
        SqlOrderByClause,
        SqlOrderByClauseChain>

        implements HavingConditionClauseTerminal<Row,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    private final SelectNode selectNode;

    /**
     * Creates a new {@code SqlHavingConditionClauseTerminal} instance.
     *
     * @param selectNode           the root select query node
     * @param node                 the current query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
    public SqlHavingConditionClauseTerminal(final SelectNode selectNode,
                                            final QueryNode node,
                                            final SelectEngineTerminal selectEngineTerminal,
                                            final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
        this.selectNode = selectNode;
    }

    @Override
    public SqlHavingConditionClause and(final String column) {
        return havingImpl(LogicOperator.AND, column, null);
    }

    @Override
    public SqlHavingConditionClause and(final ExpressionSpec expression) {
        return havingImpl(LogicOperator.AND, null, expression);
    }

    @Override
    public SqlHavingConditionClauseTerminal and(final QueryConditionBuilder<Row> query) {
        return havingImpl(LogicOperator.AND, query);
    }

    @Override
    public SqlHavingConditionClause or(final String column) {
        return havingImpl(LogicOperator.OR, column, null);
    }

    @Override
    public SqlHavingConditionClause or(final ExpressionSpec expression) {
        return havingImpl(LogicOperator.OR, null, expression);
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

    private SqlHavingConditionClause havingImpl(final LogicOperator logicOperator, final @Nullable String column, final @Nullable ExpressionSpec expression) {
        if (node instanceof HavingNode havingNode) {
            return new SqlHavingConditionClause(litebridgeContext,
                    logicOperator,
                    column,
                    expression,
                    havingNode.condition(),
                    conditionNode -> new SqlHavingConditionClauseTerminal(selectNode, havingNode.withCondition(conditionNode), selectEngineTerminal, litebridgeContext));
        }

        return new SqlHavingConditionClause(litebridgeContext,
                logicOperator,
                column,
                expression,
                null,
                conditionNode -> new SqlHavingConditionClauseTerminal(selectNode, new HavingNode(this.node, conditionNode), selectEngineTerminal, litebridgeContext));
    }

    private SqlHavingConditionClauseTerminal havingImpl(final LogicOperator logicOperator, final QueryConditionBuilder<Row> query) {
        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(selectNode, node, litebridgeContext);
        final AbstractCbConditionClauseTerminal<Row> terminal = query.apply(conditionClauseStart);
        final QueryNode conditionNode = terminal.node();

        if (node instanceof HavingNode havingNode) {
            final ConditionGroupNode groupNode = new ConditionGroupNode(havingNode.condition(), logicOperator, conditionNode);
            havingNode.withCondition(groupNode);
            return this;
        }

        final ConditionGroupNode groupNode = new ConditionGroupNode(null, logicOperator, conditionNode);
        this.node = new HavingNode(node, groupNode);
        return this;
    }
}
