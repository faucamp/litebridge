package org.litebridge.orm.api.update;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

/**
 * SQL-mode step for specifying the value of a column or expression in a SET clause.
 */
public final class SqlUpdateSetStep

        extends UpdateSetStep<Row,
        SqlUpdateStep,
        SqlUpdateWhereConditionClause,
        SqlUpdateWhereConditionClauseTerminal> {

    /**
     * Creates a new {@code SqlUpdateSetStep} instance with a column name.
     *
     * @param column            the column name
     * @param node              the current query node
     * @param updateStepCreator the function to create the update step
     */
    public SqlUpdateSetStep(final String column, final QueryNode node, final Function<QueryNode, SqlUpdateStep> updateStepCreator) {
        super(column, node, updateStepCreator);
    }

    /**
     * Creates a new {@code SqlUpdateSetStep} instance with an expression specification.
     *
     * @param expressionSpec    the expression specification
     * @param node              the current query node
     * @param updateStepCreator the function to create the update step
     */
    public SqlUpdateSetStep(final ExpressionSpec expressionSpec, final QueryNode node, final Function<QueryNode, SqlUpdateStep> updateStepCreator) {
        super(expressionSpec, node, updateStepCreator);
    }
}
