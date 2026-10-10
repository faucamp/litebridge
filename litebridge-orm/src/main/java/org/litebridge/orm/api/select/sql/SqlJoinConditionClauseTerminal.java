package org.litebridge.orm.api.select.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.CbConditionClauseTerminalInspector;
import org.litebridge.orm.api.condition.CbSqlConditionClauseTerminal;
import org.litebridge.orm.api.condition.SqlConditionClauseStart;
import org.litebridge.orm.api.condition.SqlQueryConditionBuilder;
import org.litebridge.orm.api.select.impl.AbstractJoinConditionClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Represents the terminal part of a JOIN condition clause in a SQL-based query.
 */
public final class SqlJoinConditionClauseTerminal<ReturnType> extends AbstractJoinConditionClauseTerminal<ReturnType,
        SqlJoinConditionClause<ReturnType>,
        SqlJoinConditionClauseTerminal<ReturnType>,
        SqlGroupByClauseTerminal<ReturnType>,
        SqlHavingConditionClause<ReturnType>,
        SqlHavingConditionClauseTerminal<ReturnType>,
        SqlQueryConditionBuilder<ReturnType>,
        SqlOrderByClause<ReturnType>,
        SqlOrderByClauseChain<ReturnType>>

        implements SqlJoinClauseTerminal<ReturnType> {

    private final SelectNode selectNode;

    /**
     * Creates a new instance of {@code SqlJoinConditionClauseTerminal}.
     *
     * @param selectNode           the root select query node
     * @param joinNode             the join query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
    public SqlJoinConditionClauseTerminal(final SelectNode selectNode,
                                          final JoinNode joinNode,
                                          final SelectEngineTerminal selectEngineTerminal,
                                          final LitebridgeContext litebridgeContext) {
        super(joinNode, selectEngineTerminal, litebridgeContext);
        this.selectNode = selectNode;
    }

    @Override
    public SqlJoinConditionClause<ReturnType> and(final String column) {
        return joinImpl(LogicOperator.AND, column, null);
    }

    @Override
    public SqlJoinConditionClause<ReturnType> and(final ExpressionSpec expression) {
        return joinImpl(LogicOperator.AND, null, expression);
    }

    @Override
    public SqlJoinConditionClauseTerminal<ReturnType> and(final SqlQueryConditionBuilder<ReturnType> query) {
        return joinImpl(LogicOperator.AND, query);
    }

    @Override
    public SqlJoinConditionClause<ReturnType> or(final String column) {
        return joinImpl(LogicOperator.OR, column, null);
    }

    @Override
    public SqlJoinConditionClause<ReturnType> or(final ExpressionSpec expression) {
        return joinImpl(LogicOperator.OR, null, expression);
    }

    @Override
    public SqlJoinConditionClauseTerminal<ReturnType> or(final SqlQueryConditionBuilder<ReturnType> query) {
        return joinImpl(LogicOperator.OR, query);
    }

    @Override
    public SqlJoinClause<ReturnType> join(final String table) {
        return new SqlJoinClause<>(litebridgeContext, conditionNode -> {
            final JoinNode joinNode = new JoinNode(node, Join.JoinType.INNER, null, null, table, null, null);
            joinNode.setCondition(conditionNode);
            return new SqlJoinConditionClauseTerminal<>(selectNode, joinNode, selectEngineTerminal, litebridgeContext);
        });
    }

    @Override
    public SqlWhereConditionClause<ReturnType> where(final String column) {
        return whereImpl(LogicOperator.NOOP, column, null);
    }

    @Override
    public SqlWhereConditionClause<ReturnType> where(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.NOOP, null, expression);
    }

    @Override
    public SqlGroupByClauseTerminal<ReturnType> groupBy(final String... columns) {
        return new SqlGroupByClauseTerminal<>(selectNode, columns, node(), selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlGroupByClauseTerminal<ReturnType> groupBy(final ExpressionSpec... expressions) {
        return new SqlGroupByClauseTerminal<>(selectNode, expressions, node(), selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlOrderByClause<ReturnType> orderBy(final String... columns) {
        return new SqlOrderByClause<>(columns, node(), selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlOrderByClause<ReturnType> orderBy(final ExpressionSpec... expressions) {
        return new SqlOrderByClause<>(expressions, node(), selectEngineTerminal, litebridgeContext);
    }

    private SqlWhereConditionClause<ReturnType> whereImpl(final LogicOperator logicOperator, final @Nullable String column, final @Nullable ExpressionSpec expression) {
        return new SqlWhereConditionClause<>(litebridgeContext,
                logicOperator,
                column,
                expression,
                null,
                conditionNode -> new SqlWhereConditionClauseTerminal<>(selectNode, new WhereNode(this.node, conditionNode), selectEngineTerminal, litebridgeContext));
    }

    private SqlJoinConditionClause<ReturnType> joinImpl(final LogicOperator logicOperator, final @Nullable String column, final @Nullable ExpressionSpec expression) {
        return new SqlJoinConditionClause<>(litebridgeContext,
                logicOperator,
                column,
                expression,
                joinNode.condition(),
                conditionNode -> {
                    joinNode.setCondition(conditionNode);
                    return this;
                });
    }

    private SqlJoinConditionClauseTerminal<ReturnType> joinImpl(final LogicOperator logicOperator, final SqlQueryConditionBuilder<ReturnType> query) {
        final SqlConditionClauseStart<ReturnType> conditionClauseStart = new SqlConditionClauseStart<>(selectNode, null, litebridgeContext);
        final CbSqlConditionClauseTerminal<ReturnType> terminal = query.apply(conditionClauseStart);
        final QueryNode conditionNode = CbConditionClauseTerminalInspector.getNode(terminal);

        final ConditionGroupNode groupNode = new ConditionGroupNode(joinNode.condition(), logicOperator, conditionNode);
        joinNode.setCondition(groupNode);

        return this;
    }
}
