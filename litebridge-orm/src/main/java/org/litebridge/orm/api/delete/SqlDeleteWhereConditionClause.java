package org.litebridge.orm.api.delete;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

/**
 * SQL-mode WHERE condition clause for delete operations.
 */
public final class SqlDeleteWhereConditionClause

        extends ConditionClauseImpl<Row,
        SqlDeleteWhereConditionClause,
        SqlDeleteWhereConditionClauseTerminal>

        implements DeleteWhereConditionClause<Row,
        SqlDeleteWhereConditionClause,
        SqlDeleteWhereConditionClauseTerminal> {

    /**
     * Creates a new {@code SqlDeleteWhereConditionClause} instance.
     *
     * @param litebridgeContext the Litebridge context
     * @param logicOperator     the logical operator (AND/OR)
     * @param lhsColumn         the left-hand side column name
     * @param lhsExpression     the left-hand side expression
     * @param terminalRecreator the function to create the terminal clause
     */
    public SqlDeleteWhereConditionClause(final LitebridgeContext litebridgeContext,
                                         final LogicOperator logicOperator,
                                         final @Nullable String lhsColumn,
                                         final @Nullable ExpressionSpec lhsExpression,
                                         final Function<QueryNode, SqlDeleteWhereConditionClauseTerminal> terminalRecreator) {
        super(litebridgeContext, logicOperator, lhsColumn, lhsExpression, null, terminalRecreator);
    }
}
