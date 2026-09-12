package org.litebridge.orm.api.select.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.impl.AbstractGroupByClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.GroupByNode;
import org.litebridge.orm.engine.ast.HavingNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * SQL-mode terminal clause for GROUP BY clauses.
 */
public final class SqlGroupByClauseTerminal extends AbstractGroupByClauseTerminal<Row,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    private final String table;

    /**
     * Creates a new {@code SqlGroupByClauseTerminal} instance using expressions.
     *
     * @param table                the table name
     * @param expressions          the group-by expressions
     * @param node                 the current query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
    public SqlGroupByClauseTerminal(final String table,
                                    final ExpressionSpec[] expressions,
                                    final QueryNode node,
                                    final SelectEngineTerminal selectEngineTerminal,
                                    final LitebridgeContext litebridgeContext) {
        super(expressions, new GroupByNode(node, null, expressions), selectEngineTerminal, litebridgeContext);
        this.table = table;
    }

    /**
     * Creates a new {@code SqlGroupByClauseTerminal} instance using column names.
     *
     * @param table                the table name
     * @param columns              the group-by column names
     * @param node                 the current query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
    public SqlGroupByClauseTerminal(final String table,
                                    final String[] columns,
                                    final QueryNode node,
                                    final SelectEngineTerminal selectEngineTerminal,
                                    final LitebridgeContext litebridgeContext) {
        super(columns, new GroupByNode(node, columns, null), selectEngineTerminal, litebridgeContext);
        this.table = table;
    }

    @Override
    public SqlHavingConditionClause having(final ExpressionSpec expression) {
        return new SqlHavingConditionClause(litebridgeContext,
                LogicOperator.NOOP,
                null,
                expression,
                null,
                conditionNode -> new SqlHavingConditionClauseTerminal(table, new HavingNode(this.node, conditionNode), selectEngineTerminal, litebridgeContext));
    }

    @Override
    public SqlOrderByClause orderBy(final String... columns) {
        return new SqlOrderByClause(columns, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlOrderByClause orderBy(final ExpressionSpec... expressions) {
        return new SqlOrderByClause(expressions, node, selectEngineTerminal, litebridgeContext);
    }
}
