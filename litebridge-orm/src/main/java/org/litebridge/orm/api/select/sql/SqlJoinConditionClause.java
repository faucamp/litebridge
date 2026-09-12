package org.litebridge.orm.api.select.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

/**
 * Represents a condition within a JOIN clause in a SQL-based query.
 */
public final class SqlJoinConditionClause extends ConditionClauseImpl<Row,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal>

        implements JoinConditionClause<Row,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal> {

    /**
     * Creates a new {@code SqlJoinConditionClause} instance.
     *
     * @param litebridgeContext the Litebridge context
     * @param logicOperator     the logical operator (AND/OR)
     * @param lhsField          the left-hand side column name
     * @param lhsExpression     the left-hand side expression
     * @param node              the previous query node in the chain
     * @param terminalRecreator the function to create the terminal clause
     */
    public SqlJoinConditionClause(final LitebridgeContext litebridgeContext,
                                  final LogicOperator logicOperator,
                                  final @Nullable String lhsField,
                                  final @Nullable ExpressionSpec lhsExpression,
                                  final @Nullable QueryNode node,
                                  final Function<QueryNode, SqlJoinConditionClauseTerminal> terminalRecreator) {
        super(litebridgeContext, logicOperator, lhsField, lhsExpression, node, terminalRecreator);
    }
}
