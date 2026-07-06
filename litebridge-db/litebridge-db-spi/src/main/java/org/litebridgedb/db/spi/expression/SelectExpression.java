package org.litebridgedb.db.spi.expression;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Operation;

/**
 * A select expression in a SQL query.
 */
public interface SelectExpression {

    /**
     * Creates a SQL representation of the expression, providing the immediate nesting/parent expression.
     *
     * @param operation the operation that is being executed
     * @param clause    The current clause type being evaluated
     * @param parent    The parent expression/nesting expression, or {@code null} if this is a top-level expression
     * @return the SQL representation of the expression
     */
    String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent);

    /**
     * Creates a SQL representation of the expression as a non-nested/top-level expression.
     *
     * @param operation the operation that is being executed
     * @param clause    The current clause type being evaluated
     * @return the SQL representation of the expression
     */
    default String toSql(final Operation operation, final ClauseType clause) {
        return toSql(operation, clause, null);
    }
}
