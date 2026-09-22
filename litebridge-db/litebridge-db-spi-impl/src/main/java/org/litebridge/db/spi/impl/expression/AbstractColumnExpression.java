package org.litebridge.db.spi.impl.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.impl.sql.LabelGenerator;

import java.util.Objects;

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
public abstract class AbstractColumnExpression extends AbstractAliasedExpression implements ColumnExpression {

    /**
     * The target column of this expression.
     */
    protected final Column column;

    /**
     * Creates a new {@code AbstractColumnExpression} instance.
     *
     * @param column         The target column for this expression.
     * @param alias          the alias for the expression
     * @param tableAlias     parent/source table alias
     * @param labelGenerator the label generator for rendering aliases/identifiers
     */
    protected AbstractColumnExpression(final Column column, final @Nullable String alias, final @Nullable String tableAlias, final LabelGenerator labelGenerator) {
        super(alias, tableAlias, labelGenerator);
        this.column = column;
    }

    @Override
    public Column column() {
        return column;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final AbstractColumnExpression that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(column, that.column);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), column);
    }
}
