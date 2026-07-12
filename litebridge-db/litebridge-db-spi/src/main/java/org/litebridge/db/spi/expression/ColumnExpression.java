package org.litebridge.db.spi.expression;

import org.litebridge.db.spi.Column;

/**
 * A column-based expression within a SQL query.
 * <p>
 * This interface serves as a contract for defining expressions that specifically
 * target database columns. It extends {@code SelectExpression}, requiring the
 * implementation of methods to produce SQL representations and mandates the
 * ability to retrieve the underlying {@code Column} instance associated with
 * the expression.
 */
public interface ColumnExpression extends SelectExpression {

    /**
     * Retrieves the target column of this expression.
     *
     * @return The target column.
     */
    Column column();
}
