package org.litebridge.orm.api.select.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.WhereConditionClause;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

/**
 * SQL-mode where condition clause.
 */
public final class SqlWhereConditionClause
        extends ConditionClauseImpl<Row,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal>

        implements WhereConditionClause<Row,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal,
        SqlGroupByClauseTerminal,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    /**
     * Creates a new {@code SqlWhereConditionClause} instance.
     *
     * @param litebridgeContext the Litebridge context
     * @param logicOperator     the logical operator (AND/OR)
     * @param lhsField          the left-hand side column or field name
     * @param lhsExpression     the left-hand side expression
     * @param node              the previous query node in the chain
     * @param terminalRecreator the function to create the terminal clause
     */
    public SqlWhereConditionClause(final LitebridgeContext litebridgeContext,
                                   final LogicOperator logicOperator,
                                   final @Nullable String lhsField,
                                   final @Nullable ExpressionSpec lhsExpression,
                                   final @Nullable QueryNode node,
                                   final Function<QueryNode, SqlWhereConditionClauseTerminal> terminalRecreator) {
        super(litebridgeContext, logicOperator, lhsField, lhsExpression, node, terminalRecreator);
    }
}
