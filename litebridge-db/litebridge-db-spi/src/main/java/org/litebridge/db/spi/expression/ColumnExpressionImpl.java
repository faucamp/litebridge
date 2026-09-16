package org.litebridge.db.spi.expression;

import org.jspecify.annotations.Nullable;
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
    protected final @Nullable String alias;
    protected final @Nullable String tableAlias;

    /**
     * Constructor.
     *
     * @param column The target column for this expression.
     */
    protected ColumnExpressionImpl(final Column column, final @Nullable String alias, final @Nullable String tableAlias) {
        this.column = column;
        this.alias = alias;
        this.tableAlias = tableAlias;
    }

    @Override
    public Column column() {
        return column;
    }

    @Override
    public @Nullable String alias() {
        return alias;
    }

    @Override
    public @Nullable String tableAlias() {
        return tableAlias;
    }
}
