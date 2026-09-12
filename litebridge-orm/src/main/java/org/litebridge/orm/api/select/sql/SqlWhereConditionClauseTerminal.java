package org.litebridge.orm.api.select.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.WhereConditionClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractWhereClauseTerminal;
import org.litebridge.orm.api.condition.SqlConditionClauseStart;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * SQL-mode where condition clause terminal.
 */
public final class SqlWhereConditionClauseTerminal
        extends AbstractWhereClauseTerminal<Row,
        SqlGroupByClauseTerminal,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain>

        implements WhereConditionClauseTerminal<Row,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal,
        SqlGroupByClauseTerminal,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    private final String table;

    /**
     * Constructs a new {@code SqlWhereConditionClauseTerminal}.
     *
     * @param table                the table name
     * @param node                 the current query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
    public SqlWhereConditionClauseTerminal(final String table,
                                           final QueryNode node,
                                           final SelectEngineTerminal selectEngineTerminal,
                                           final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
        this.table = table;
    }

    @Override
    public SqlWhereConditionClause and(final String column) {
        return whereImpl(LogicOperator.AND, column, null);
    }

    @Override
    public SqlWhereConditionClause and(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.AND, null, expression);
    }

    @Override
    public SqlWhereConditionClauseTerminal and(final QueryConditionBuilder<Row> query) {
        return whereImpl(LogicOperator.AND, query);
    }

    @Override
    public SqlWhereConditionClause or(final String column) {
        return whereImpl(LogicOperator.OR, column, null);
    }

    @Override
    public SqlWhereConditionClause or(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.OR, null, expression);
    }

    @Override
    public SqlWhereConditionClauseTerminal or(final QueryConditionBuilder<Row> query) {
        return whereImpl(LogicOperator.OR, query);
    }

    @Override
    public SqlGroupByClauseTerminal groupBy(final String... columns) {
        return new SqlGroupByClauseTerminal(table, columns, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlGroupByClauseTerminal groupBy(final ExpressionSpec... expressions) {
        return new SqlGroupByClauseTerminal(table, expressions, node, selectEngineTerminal, litebridgeContext);
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
                    node -> new SqlWhereConditionClauseTerminal(table, whereNode.withCondition(node), selectEngineTerminal, litebridgeContext));
        }

        return new SqlWhereConditionClause(litebridgeContext,
                logicOperator,
                column,
                expression,
                null,
                conditionNode -> new SqlWhereConditionClauseTerminal(table, new WhereNode(this.node, conditionNode), selectEngineTerminal, litebridgeContext));
    }

    private SqlWhereConditionClauseTerminal whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<Row> query) {
        if (!(node instanceof WhereNode whereNode)) {
            throw new IllegalArgumentException("AST error: Expected a WhereNode but got " + node);
        }

        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(table, node, litebridgeContext);
        final AbstractCbConditionClauseTerminal<Row> terminal = query.apply(conditionClauseStart);

        whereNode.withCondition(new ConditionGroupNode(whereNode.condition(), logicOperator, terminal.node()));
        return this;
    }
}
