package org.litebridge.orm.api.select.sql;

import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.SqlQueryConditionBuilder;
import org.litebridge.orm.api.select.impl.AbstractJoinClause;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.ProtoExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import java.util.function.Function;

/**
 * Represents a JOIN clause in a SQL-based query.
 */
public final class SqlJoinClause<ReturnType>
        extends AbstractJoinClause<ReturnType,
        SqlJoinConditionClause<ReturnType>,
        SqlJoinConditionClauseTerminal<ReturnType>,
        SqlQueryConditionBuilder<ReturnType>> {

    private final Function<QueryNode, SqlJoinConditionClauseTerminal<ReturnType>> terminalCreator;

    /**
     * Creates a new instance of {@code SqlJoinClause}.
     *
     * @param litebridgeContext the Litebridge context
     * @param terminalCreator   the function to create the terminal clause
     */
    public SqlJoinClause(final LitebridgeContext litebridgeContext,
                         final Function<QueryNode, SqlJoinConditionClauseTerminal<ReturnType>> terminalCreator) {
        super(litebridgeContext);
        this.terminalCreator = terminalCreator;
    }

    /**
     * Adds a join ON condition to the current join clause based on the specified column.
     *
     * @param column the name of the column to be used in the join condition
     * @return an instance of the join condition clause to allow further configuration
     */
    public SqlJoinConditionClause<ReturnType> on(final String column) {
        return new SqlJoinConditionClause<>(litebridgeContext,
                LogicOperator.NOOP,
                column,
                null,
                null,
                terminalCreator);
    }

    /**
     * Adds a join ON condition based on a query expression.
     *
     * @param expression the expression to use for the join condition
     * @return an instance of the join condition clause to allow further configuration
     */
    public SqlJoinConditionClause<ReturnType> on(final ExpressionSpec expression) {
        return switch (expression) {
            case ProtoExpressionSpec protoExpressionSpec -> on(protoExpressionSpec.column());
            case SelectColumnSpec selectColumnSpec -> on(selectColumnSpec.getColumn().name());
            default -> throw new IllegalArgumentException("Unsupported JOIN ON expression: " + expression);
        };
    }

    /**
     * Adds a join USING condition to the current join clause using the specified column.
     * This method simplifies the join condition by specifying a single column that is
     * shared between two tables in the join.
     *
     * @param column the name of the column to be used for the join condition
     * @return an instance of the terminal join condition clause to finalize the join conditions
     */
    public SqlJoinConditionClauseTerminal<ReturnType> using(final String column) {
        return on(column).using(column);
    }
}
