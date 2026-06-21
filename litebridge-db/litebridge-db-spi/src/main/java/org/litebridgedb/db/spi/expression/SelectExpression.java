package org.litebridgedb.db.spi.expression;

import org.litebridgedb.db.spi.Operation;

/**
 * A select expression in a SQL query.
 */
public interface SelectExpression {

    /**
     * Creates a SQL representation of the expression.
     *
     * @param operation the operation that is being executed
     * @return the SQL representation of the expression
     */
    String toSql(final Operation operation);
}
