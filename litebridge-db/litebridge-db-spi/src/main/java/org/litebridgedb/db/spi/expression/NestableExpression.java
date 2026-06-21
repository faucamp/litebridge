package org.litebridgedb.db.spi.expression;

import org.litebridgedb.db.spi.Column;

/**
 * Abstract base class for nestable SQL function expressions.
 * <p>
 * This class provides a foundation for creating SQL function expressions
 * by encapsulating a {@code Column} and offering a method to access it.
 * It is designed to be extended by concrete implementations to define
 * specific behaviors and SQL representations.
 * <p>
 * Classes that extend {@code ColumnExpression} are expected to implement
 * the {@code toSql} method from the {@code SelectExpression} interface.
 */
public abstract class NestableExpression extends ColumnExpression {

    /**
     * The encapsulated target column expression of this expression.
     */
    protected final ColumnExpression target;

    /**
     * Constructor.
     *
     * @param target The encapsulated target column expression for this expression.
     */
    protected NestableExpression(final ColumnExpression target) {
        super(target.column());
        this.target = target;
    }

    /**
     * Retrieves the target column of this expression.
     *
     * @return The target column.
     */
    @Override
    public final Column column() {
        return target.column();
    }
}
