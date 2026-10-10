package org.litebridge.orm.api.select.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.SqlQueryConditionBuilder;
import org.litebridge.orm.api.select.impl.AbstractFromClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * SQL-mode terminal clause for FROM clauses.
 */
public final class SqlFromClauseTerminal<ReturnType> extends AbstractFromClauseTerminal<ReturnType,
        SqlJoinClause<ReturnType>,
        SqlJoinConditionClause<ReturnType>,
        SqlJoinConditionClauseTerminal<ReturnType>,
        SqlWhereConditionClause<ReturnType>,
        SqlWhereConditionClauseTerminal<ReturnType>,
        SqlGroupByClauseTerminal<ReturnType>,
        SqlHavingConditionClause<ReturnType>,
        SqlHavingConditionClauseTerminal<ReturnType>,
        SqlQueryConditionBuilder<ReturnType>,
        SqlOrderByClause<ReturnType>,
        SqlOrderByClauseChain<ReturnType>>

        implements SqlJoinClauseTerminal<ReturnType> {

    private final SelectNode selectNode;

    /**
     * Creates a new {@code SqlFromClauseTerminal} instance.
     *
     * @param selectNode           the SELECT query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
    public SqlFromClauseTerminal(final SelectNode selectNode,
                                 final SelectEngineTerminal selectEngineTerminal,
                                 final LitebridgeContext litebridgeContext) {
        super(selectNode, selectEngineTerminal, litebridgeContext);
        this.selectNode = selectNode;
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
}
