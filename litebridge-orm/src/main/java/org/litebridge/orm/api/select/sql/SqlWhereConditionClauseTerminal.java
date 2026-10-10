package org.litebridge.orm.api.select.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.CbConditionClauseTerminalInspector;
import org.litebridge.orm.api.condition.CbSqlConditionClauseTerminal;
import org.litebridge.orm.api.condition.SqlConditionClauseStart;
import org.litebridge.orm.api.condition.SqlQueryConditionBuilder;
import org.litebridge.orm.api.select.WhereConditionClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractWhereClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * SQL-mode where condition clause terminal.
 */
public final class SqlWhereConditionClauseTerminal<ReturnType>
        extends AbstractWhereClauseTerminal<ReturnType,
        SqlGroupByClauseTerminal<ReturnType>,
        SqlHavingConditionClause<ReturnType>,
        SqlHavingConditionClauseTerminal<ReturnType>,
        SqlQueryConditionBuilder<ReturnType>,
        SqlOrderByClause<ReturnType>,
        SqlOrderByClauseChain<ReturnType>>

        implements WhereConditionClauseTerminal<ReturnType,
        SqlWhereConditionClause<ReturnType>,
        SqlWhereConditionClauseTerminal<ReturnType>,
        SqlGroupByClauseTerminal<ReturnType>,
        SqlHavingConditionClause<ReturnType>,
        SqlHavingConditionClauseTerminal<ReturnType>,
        SqlQueryConditionBuilder<ReturnType>,
        SqlOrderByClause<ReturnType>,
        SqlOrderByClauseChain<ReturnType>> {

    private final SelectNode selectNode;

    /**
     * Constructs a new {@code SqlWhereConditionClauseTerminal}.
     *
     * @param selectNode           the root select query node
     * @param node                 the current query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
    public SqlWhereConditionClauseTerminal(final SelectNode selectNode,
                                           final QueryNode node,
                                           final SelectEngineTerminal selectEngineTerminal,
                                           final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
        this.selectNode = selectNode;
    }

    @Override
    public SqlWhereConditionClause<ReturnType> and(final String column) {
        return whereImpl(LogicOperator.AND, column, null);
    }

    @Override
    public SqlWhereConditionClause<ReturnType> and(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.AND, null, expression);
    }

    @Override
    public SqlWhereConditionClauseTerminal<ReturnType> and(final SqlQueryConditionBuilder<ReturnType> query) {
        return whereImpl(LogicOperator.AND, query);
    }

    @Override
    public SqlWhereConditionClause<ReturnType> or(final String column) {
        return whereImpl(LogicOperator.OR, column, null);
    }

    @Override
    public SqlWhereConditionClause<ReturnType> or(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.OR, null, expression);
    }

    @Override
    public SqlWhereConditionClauseTerminal<ReturnType> or(final SqlQueryConditionBuilder<ReturnType> query) {
        return whereImpl(LogicOperator.OR, query);
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
        if (node instanceof WhereNode whereNode) {
            return new SqlWhereConditionClause<>(litebridgeContext,
                    logicOperator,
                    column,
                    expression,
                    whereNode.condition(),
                    node -> new SqlWhereConditionClauseTerminal<>(selectNode, whereNode.withCondition(node), selectEngineTerminal, litebridgeContext));
        }

        return new SqlWhereConditionClause<>(litebridgeContext,
                logicOperator,
                column,
                expression,
                null,
                conditionNode -> new SqlWhereConditionClauseTerminal<>(selectNode, new WhereNode(this.node, conditionNode), selectEngineTerminal, litebridgeContext));
    }

    private SqlWhereConditionClauseTerminal<ReturnType> whereImpl(final LogicOperator logicOperator, final SqlQueryConditionBuilder<ReturnType> query) {
        if (!(node instanceof WhereNode whereNode)) {
            throw new IllegalArgumentException("AST error: Expected a WhereNode but got " + node);
        }

        final SqlConditionClauseStart<ReturnType> conditionClauseStart = new SqlConditionClauseStart<>(selectNode, null, litebridgeContext);
        final CbSqlConditionClauseTerminal<ReturnType> terminal = query.apply(conditionClauseStart);

        whereNode.withCondition(new ConditionGroupNode(whereNode.condition(), logicOperator, CbConditionClauseTerminalInspector.getNode(terminal)));
        return this;
    }
}
