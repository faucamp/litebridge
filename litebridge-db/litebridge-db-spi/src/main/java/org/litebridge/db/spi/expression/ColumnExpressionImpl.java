package org.litebridge.db.spi.expression;

import org.litebridge.db.spi.Column;

/**
 * Abstract base class for column-specific SQL expressions.
 * <p>
 * This class provides a foundation for creating SQL column expressions
 * by encapsulating a {@code Column} and offering a method to access it.
 * It is designed to be extended by concrete implementations to define
 * specific behaviors and SQL representations.
 * <p>
 * Classes that extend {@code ColumnExpression} are expected to implement
 * the {@code toSql} method from the {@code SelectExpression} interface.
 */
public abstract class ColumnExpressionImpl implements ColumnExpression {

    /**
     * The target column of this expression.
     */
    protected final Column column;

    /**
     * Constructor.
     *
     * @param column The target column for this expression.
     */
    protected ColumnExpressionImpl(final Column column) {
        this.column = column;
    }

    @Override
    public Column column() {
        return column;
    }
}
