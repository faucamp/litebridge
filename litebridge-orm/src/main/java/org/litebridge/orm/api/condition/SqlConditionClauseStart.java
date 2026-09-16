package org.litebridge.orm.api.condition;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Start of a SQL-based condition clause.
 */
public class SqlConditionClauseStart extends AbstractConditionClauseStart<Row> {

    private final SelectNode selectNode;

    /**
     * Creates a new SQL condition clause start.
     * <p>
     * This is used for non-SELECT operations.
     *
     * @param table             the target table name
     * @param node              the current query node
     * @param litebridgeContext the Litebridge context
     */
    public SqlConditionClauseStart(final String table,
                                   final @Nullable QueryNode node,
                                   final LitebridgeContext litebridgeContext) {
        super(node, litebridgeContext);
        // Not a real select node, just a placeholder
        this.selectNode = new SelectNode(table, null, null, null, null, null, null, null);
    }

    /**
     * Creates a new SQL condition clause start.
     *
     * @param selectNode        the root select query node
     * @param node              the current query node
     * @param litebridgeContext the Litebridge context
     */
    public SqlConditionClauseStart(final SelectNode selectNode,
                                   final @Nullable QueryNode node,
                                   final LitebridgeContext litebridgeContext) {
        super(node, litebridgeContext);
        this.selectNode = selectNode;
    }

    @Override
    public CbSqlConditionClause where(final String column) {
        return new CbSqlConditionClause(selectNode, litebridgeContext, LogicOperator.NOOP, column, null, node,
                conditionNode -> new CbSqlConditionClauseTerminal(selectNode, conditionNode, litebridgeContext));
    }


    @Override
    public AbstractCbConditionClause<Row> where(final ExpressionSpec expression) {
        return new CbSqlConditionClause(selectNode, litebridgeContext, LogicOperator.NOOP, null, expression, node,
                conditionNode -> new CbSqlConditionClauseTerminal(selectNode, conditionNode, litebridgeContext));
    }
}
