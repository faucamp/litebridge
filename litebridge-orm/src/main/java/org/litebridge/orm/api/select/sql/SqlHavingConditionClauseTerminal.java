package org.litebridge.orm.api.select.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.CbConditionClauseTerminalInspector;
import org.litebridge.orm.api.condition.CbSqlConditionClauseTerminal;
import org.litebridge.orm.api.condition.SqlConditionClauseStart;
import org.litebridge.orm.api.condition.SqlQueryConditionBuilder;
import org.litebridge.orm.api.select.HavingConditionClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractHavingClauseTerminal;
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
public final class SqlHavingConditionClauseTerminal<ReturnType>
        extends AbstractHavingClauseTerminal<ReturnType,
        SqlOrderByClause<ReturnType>,
        SqlOrderByClauseChain<ReturnType>>

        implements HavingConditionClauseTerminal<ReturnType,
        SqlHavingConditionClause<ReturnType>,
        SqlHavingConditionClauseTerminal<ReturnType>,
        SqlQueryConditionBuilder<ReturnType>,
        SqlOrderByClause<ReturnType>,
        SqlOrderByClauseChain<ReturnType>> {

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
    public SqlHavingConditionClause<ReturnType> and(final String column) {
        return havingImpl(LogicOperator.AND, column, null);
    }

    @Override
    public SqlHavingConditionClause<ReturnType> and(final ExpressionSpec expression) {
        return havingImpl(LogicOperator.AND, null, expression);
    }

    @Override
    public SqlHavingConditionClauseTerminal<ReturnType> and(final SqlQueryConditionBuilder<ReturnType> query) {
        return havingImpl(LogicOperator.AND, query);
    }

    @Override
    public SqlHavingConditionClause<ReturnType> or(final String column) {
        return havingImpl(LogicOperator.OR, column, null);
    }

    @Override
    public SqlHavingConditionClause<ReturnType> or(final ExpressionSpec expression) {
        return havingImpl(LogicOperator.OR, null, expression);
    }

    @Override
    public SqlHavingConditionClauseTerminal<ReturnType> or(final SqlQueryConditionBuilder<ReturnType> query) {
        return havingImpl(LogicOperator.OR, query);
    }

    @Override
    public SqlOrderByClause<ReturnType> orderBy(final String... columns) {
        return new SqlOrderByClause<>(columns, node(), selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlOrderByClause<ReturnType> orderBy(final ExpressionSpec... expressions) {
        return new SqlOrderByClause<>(expressions, node(), selectEngineTerminal, litebridgeContext);
    }

    private SqlHavingConditionClause<ReturnType> havingImpl(final LogicOperator logicOperator, final @Nullable String column, final @Nullable ExpressionSpec expression) {
        if (node instanceof HavingNode havingNode) {
            return new SqlHavingConditionClause<>(litebridgeContext,
                    logicOperator,
                    column,
                    expression,
                    havingNode.condition(),
                    conditionNode -> new SqlHavingConditionClauseTerminal<>(selectNode, havingNode.withCondition(conditionNode), selectEngineTerminal, litebridgeContext));
        }

        return new SqlHavingConditionClause<>(litebridgeContext,
                logicOperator,
                column,
                expression,
                null,
                conditionNode -> new SqlHavingConditionClauseTerminal<>(selectNode, new HavingNode(this.node, conditionNode), selectEngineTerminal, litebridgeContext));
    }

    private SqlHavingConditionClauseTerminal<ReturnType> havingImpl(final LogicOperator logicOperator, final SqlQueryConditionBuilder<ReturnType> query) {
        final SqlConditionClauseStart<ReturnType> conditionClauseStart = new SqlConditionClauseStart<>(selectNode, null, litebridgeContext);
        final CbSqlConditionClauseTerminal<ReturnType> terminal = query.apply(conditionClauseStart);
        final QueryNode conditionNode = CbConditionClauseTerminalInspector.getNode(terminal);

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
