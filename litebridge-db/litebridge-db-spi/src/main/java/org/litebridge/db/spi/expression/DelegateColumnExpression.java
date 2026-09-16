package org.litebridge.db.spi.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;

/**
 * Abstract base class for nesting query expressions such as SQL functions.
 * <p>
 * This class provides a foundation for creating SQL function expressions
 * by encapsulating a {@code Column} and offering a method to access it.
 * It is designed to be extended by concrete implementations to define
 * specific behaviors and SQL representations.
 * <p>
 * Classes that extend {@code ColumnExpression} are expected to implement
 * the {@code toSql} method from the {@code SelectExpression} interface.
 */
public abstract class DelegateColumnExpression implements ColumnExpression, DelegateExpression {

    /**
     * The encapsulated target column expression of this expression.
     */
    protected final ColumnExpression target;
    protected final @Nullable String alias;

    /**
     * Constructor.
     *
     * @param target The encapsulated target column expression for this expression.
     */
    protected DelegateColumnExpression(final ColumnExpression target, final @Nullable String alias) {
        this.target = target;
        this.alias = alias;
    }

    @Override
    public SelectExpression target() {
        return target;
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

    @Override
    public @Nullable String alias() {
        return alias;
    }

    @Override
    public @Nullable String tableAlias() {
        return target.tableAlias();
    }
}
