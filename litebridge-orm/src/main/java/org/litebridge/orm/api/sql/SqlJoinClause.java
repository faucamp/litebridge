package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.impl.AbstractJoinClause;
import org.litebridge.orm.api.sql.condition.SqlConditionClauseStart;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.ProtoExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import java.util.function.Function;

public final class SqlJoinClause extends AbstractJoinClause<Row,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal> {

    private final String selectedTable;
    private final Function<QueryNode, SqlJoinConditionClauseTerminal> terminalCreator;

    public SqlJoinClause(final String selectedTable,
                         final QueryNode node,
                         final LitebridgeContext litebridgeContext,
                         final Function<QueryNode, SqlJoinConditionClauseTerminal> terminalCreator) {
        super(node, litebridgeContext);
        this.terminalCreator = terminalCreator;
        this.selectedTable = selectedTable;
    }

    /**
     * Adds a join ON condition to the current join clause based on the specified column.
     *
     * @param column the name of the column to be used in the join condition
     * @return an instance of the join condition clause to allow further configuration
     */
    public SqlJoinConditionClause on(final String column) {
        return new SqlJoinConditionClause(litebridgeContext,
                LogicOperator.NOOP,
                column,
                null,
                node,
                terminalCreator);
    }

    /**
     * Adds a join ON condition based on a query expression.
     *
     * @param expression the expression to use for the join condition
     * @return an instance of the join condition clause to allow further configuration
     */
    public SqlJoinConditionClause on(final ExpressionSpec expression) {
        return switch (expression) {
            case ProtoExpressionSpec protoExpressionSpec -> on(protoExpressionSpec.column());
            case SelectColumnSpec selectColumnSpec -> on(selectColumnSpec.getColumn().name());
            default -> throw new IllegalArgumentException("Unsupported JOIN ON expression: " + expression);
        };
    }

    /**
     * Adds a join ON condition based on a query condition builder.
     *
     * @param builder the builder for the join condition
     * @return an instance of the join condition clause to allow further configuration
     */
    public SqlJoinConditionClauseTerminal on(final QueryConditionBuilder<Row> builder) {
        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(selectedTable, node, litebridgeContext);
        final AbstractCbConditionClauseTerminal<Row> terminal = builder.apply(conditionClauseStart);
        final QueryNode conditionNode = terminal.node();

        final ConditionGroupNode groupNode = new ConditionGroupNode(null, LogicOperator.NOOP, conditionNode);
        return terminalCreator.apply(groupNode);
    }

    /**
     * Adds a join USING condition to the current join clause using the specified column.
     * This method simplifies the join condition by specifying a single column that is
     * shared between two tables in the join.
     *
     * @param column the name of the column to be used for the join condition
     * @return an instance of the terminal join condition clause to finalize the join conditions
     */
    public SqlJoinConditionClauseTerminal using(final String column) {
        return on(column).using(column);
    }
}
