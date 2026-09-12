package org.litebridge.orm.api.select.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.HavingConditionClause;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

/**
 * Represents a HAVING condition clause for SQL queries.
 */
public final class SqlHavingConditionClause
        extends ConditionClauseImpl<Row,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal>

        implements HavingConditionClause<Row,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    /**
     * Creates a new {@code SqlHavingConditionClause} instance.
     *
     * @param litebridgeContext the Litebridge context
     * @param logicOperator     the logical operator (AND/OR)
     * @param lhsColumn         the left-hand side column name
     * @param lhsExpression     the left-hand side expression
     * @param node              the previous query node in the chain
     * @param terminalRecreator the function to create the terminal clause
     */
    public SqlHavingConditionClause(final LitebridgeContext litebridgeContext,
                                    final LogicOperator logicOperator,
                                    final @Nullable String lhsColumn,
                                    final @Nullable ExpressionSpec lhsExpression,
                                    final @Nullable QueryNode node,
                                    final Function<QueryNode, SqlHavingConditionClauseTerminal> terminalRecreator) {
        super(litebridgeContext, logicOperator, lhsColumn, lhsExpression, node, terminalRecreator);
    }
}
