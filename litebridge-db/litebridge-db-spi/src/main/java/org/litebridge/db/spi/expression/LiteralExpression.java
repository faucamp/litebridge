package org.litebridge.db.spi.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;

import java.util.Collection;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * An encapsulated literal value in a query expression.
 */
public class LiteralExpression implements SelectExpression {

    private final @Nullable Object value;
    private final @Nullable String alias;
    private final boolean parameter;

    /**
     * Constructs a new {@code LiteralExpression} with the given value.
     *
     * @param value the literal value to be represented
     */
    public LiteralExpression(final @Nullable Object value, final @Nullable String alias) {
        this(value, alias, false);
    }

    /**
     * Constructs a new {@code LiteralExpression} with the given value and parameter flag.
     *
     * @param value     the literal value to be represented
     * @param parameter whether this literal should be treated as a bind parameter
     */
    public LiteralExpression(final @Nullable Object value, final @Nullable String alias, final boolean parameter) {
        this.value = value;
        this.alias = alias;
        this.parameter = parameter;
    }

    /**
     * Retrieves the value of this literal expression.
     *
     * @return the literal value encapsulated by this expression
     */
    public @Nullable Object value() {
        return value;
    }

    public @Nullable String alias() {
        return alias;
    }

    /**
     * Returns whether this literal should be treated as a bind parameter.
     *
     * @return {@code true} if it's a parameter, {@code false} otherwise
     */
    public boolean isParameter() {
        return parameter;
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        final String valueStr;

        if (parameter) {
            return toBindValueSql(operation);
        } else if (value == null) {
            valueStr = "NULL";
        } else if (value instanceof Collection collection) {
            final StringJoiner joiner = new StringJoiner(", ");

            for (final Object element : collection) {
                joiner.add(element.toString());
            }

            valueStr = joiner.toString();
        } else if (value instanceof String string) {
            valueStr = "'" + string.replace("'", "''") + "'";
        } else {
            valueStr = value.toString();
        }

        if (alias != null) {
            return valueStr + " AS " + alias;
        }

        return valueStr;
    }

    /**
     * Generates a SQL fragment with a placeholder for a bind value.
     *
     * @param operation the database operation context
     * @return the SQL fragment with bind placeholders
     */
    public String toBindValueSql(final Operation operation) {
        if (value == null) {
            return "?";
        } else if (value instanceof Collection collection) {
            final StringJoiner joiner = new StringJoiner(", ");

            for (final Object element : collection) {
                joiner.add("?");
            }

            return joiner.toString();
        } else {
            return "?";
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final LiteralExpression that)) return false;
        return parameter == that.parameter && Objects.equals(value, that.value) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, alias, parameter);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LiteralExpression.class.getSimpleName() + "[", "]")
                .add("value=" + value)
                .add("alias='" + alias + "'")
                .add("parameter=" + parameter)
                .toString();
    }
}
