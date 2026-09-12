package org.litebridge.orm.api.condition;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

/**
 * Implementation of a condition clause for SQL-based queries.
 */
public class CbSqlConditionClause extends AbstractCbConditionClause<Row> {

    private final String table;

    /**
     * Constructs a new {@code CbSqlConditionClause}.
     *
     * @param table             the target table name
     * @param litebridgeContext the Litebridge context
     * @param logicOperator     the logical operator (AND/OR)
     * @param lhsColumn         the left-hand side column name
     * @param lhsExpression     the left-hand side expression
     * @param node              the previous node in the chain
     * @param terminalCreator   the function to create the terminal clause
     */
    public CbSqlConditionClause(final String table,
                                final LitebridgeContext litebridgeContext,
                                final LogicOperator logicOperator,
                                final @Nullable String lhsColumn,
                                final @Nullable ExpressionSpec lhsExpression,
                                final @Nullable QueryNode node,
                                final Function<QueryNode, AbstractCbConditionClauseTerminal<Row>> terminalCreator) {
        super(litebridgeContext, logicOperator, lhsColumn, lhsExpression, node, terminalCreator);
        this.table = table;
    }

    @Override
    protected AbstractCbConditionClauseTerminal<Row> createCbConditionClauseTerminal(final QueryNode conditionNode) {
        return new CbSqlConditionClauseTerminal(table, conditionNode, litebridgeContext);
    }
}
