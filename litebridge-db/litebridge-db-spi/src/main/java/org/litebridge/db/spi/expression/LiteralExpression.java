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
    private final boolean parameter;

    /**
     * Constructs a new {@code LiteralExpression} with the given value.
     *
     * @param value the literal value to be represented
     */
    public LiteralExpression(final @Nullable Object value) {
        this(value, false);
    }

    /**
     * Constructs a new {@code LiteralExpression} with the given value and parameter flag.
     *
     * @param value     the literal value to be represented
     * @param parameter whether this literal should be treated as a bind parameter
     */
    public LiteralExpression(final @Nullable Object value, final boolean parameter) {
        this.value = value;
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
        if (parameter) {
            return toBindValueSql(operation);
        }
        if (value == null) {
            return "NULL";
//        } else if (value.getClass().isArray()) {
//            final Object[] array = (Object[]) value;
//
//            final StringJoiner joiner = new StringJoiner(", ");
//
//            for (final Object element : array) {
//                joiner.add(element.toString());
//            }
//
//            return joiner.toString();
        } else if (value instanceof Collection collection) {
            final StringJoiner joiner = new StringJoiner(", ");

            for (final Object element : collection) {
                joiner.add(element.toString());
            }

            return joiner.toString();
        } else {
            return value.toString();
        }
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
//        } else if (value.getClass().isArray()) {
//            final Object[] array = (Object[]) value;
//
//            final StringJoiner joiner = new StringJoiner(", ");
//
//            for (final Object element : array) {
//                joiner.add("?");
//            }
//
//
//            return joiner.toString();
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
        if (this.parameter != that.parameter) return false;
        if (this.parameter) return true; // Structurally equal if both are parameters
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        if (parameter) return 31; // Constant hash for all parameters
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LiteralExpression.class.getSimpleName() + "[", "]")
                .add("value=" + value)
                .toString();
    }
}
