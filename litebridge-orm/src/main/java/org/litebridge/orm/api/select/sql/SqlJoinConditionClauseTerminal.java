package org.litebridge.orm.api.select.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.impl.AbstractJoinConditionClauseTerminal;
import org.litebridge.orm.api.condition.SqlConditionClauseStart;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Represents the terminal part of a JOIN condition clause in a SQL-based query.
 */
public final class SqlJoinConditionClauseTerminal extends AbstractJoinConditionClauseTerminal<Row,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal,
        SqlGroupByClauseTerminal,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain>

        implements SqlJoinClauseTerminal {

    private final String selectedTable;

    /**
     * Creates a new instance of {@code SqlJoinConditionClauseTerminal}.
     *
     * @param selectedTable        the selected table name
     * @param joinNode             the join query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
    public SqlJoinConditionClauseTerminal(final String selectedTable,
                                          final JoinNode joinNode,
                                          final SelectEngineTerminal selectEngineTerminal,
                                          final LitebridgeContext litebridgeContext) {
        super(joinNode, selectEngineTerminal, litebridgeContext);
        this.selectedTable = selectedTable;
    }

    @Override
    public SqlJoinConditionClause and(final String column) {
        return joinImpl(LogicOperator.AND, column, null);
    }

    @Override
    public SqlJoinConditionClause and(final ExpressionSpec expression) {
        return joinImpl(LogicOperator.AND, null, expression);
    }

    @Override
    public SqlJoinConditionClauseTerminal and(final QueryConditionBuilder<Row> query) {
        return joinImpl(LogicOperator.AND, query);
    }

    @Override
    public SqlJoinConditionClause or(final String column) {
        return joinImpl(LogicOperator.OR, column, null);
    }

    @Override
    public SqlJoinConditionClause or(final ExpressionSpec expression) {
        return joinImpl(LogicOperator.OR, null, expression);
    }

    @Override
    public SqlJoinConditionClauseTerminal or(final QueryConditionBuilder<Row> query) {
        return joinImpl(LogicOperator.OR, query);
    }

    @Override
    public SqlJoinClause join(final String table) {
        return new SqlJoinClause(table, null, litebridgeContext, conditionNode -> {
            final JoinNode joinNode = new JoinNode(node, "INNER", null, table);
            joinNode.setCondition(conditionNode);
            return new SqlJoinConditionClauseTerminal(table, joinNode, selectEngineTerminal, litebridgeContext);
        });
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
        return new SqlGroupByClauseTerminal(selectedTable, columns, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlGroupByClauseTerminal groupBy(final ExpressionSpec... expressions) {
        return new SqlGroupByClauseTerminal(selectedTable, expressions, node, selectEngineTerminal, litebridgeContext);
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
                    node -> new SqlWhereConditionClauseTerminal(selectedTable, whereNode.withCondition(node), selectEngineTerminal, litebridgeContext));
        }

        return new SqlWhereConditionClause(litebridgeContext,
                logicOperator,
                column,
                expression,
                null,
                conditionNode -> new SqlWhereConditionClauseTerminal(selectedTable, new WhereNode(this.node, conditionNode), selectEngineTerminal, litebridgeContext));
    }

    private SqlJoinConditionClause joinImpl(final LogicOperator logicOperator, final @Nullable String column, final @Nullable ExpressionSpec expression) {
        return new SqlJoinConditionClause(litebridgeContext,
                logicOperator,
                column,
                expression,
                joinNode.condition(),
                conditionNode -> {
                    joinNode.setCondition(conditionNode);
                    return this;
                });
    }

    private SqlJoinConditionClauseTerminal joinImpl(final LogicOperator logicOperator, final QueryConditionBuilder<Row> query) {
        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(selectedTable, null, litebridgeContext);
        final AbstractCbConditionClauseTerminal<Row> terminal = query.apply(conditionClauseStart);
        final QueryNode conditionNode = terminal.node();

        final ConditionGroupNode groupNode = new ConditionGroupNode(joinNode.condition(), logicOperator, conditionNode);
        joinNode.setCondition(groupNode);

        return this;
    }
}
