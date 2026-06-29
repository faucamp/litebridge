package org.litebridgedb.db.spi.expression;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Operation;

import java.util.Collection;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * An encapsulated literal value in a query expression.
 */
public class LiteralExpression implements SelectExpression {

    private final @Nullable Object value;

    /**
     * Constructs a new {@code LiteralExpression} with the given value.
     *
     * @param value the literal value to be represented
     */
    public LiteralExpression(final @Nullable Object value) {
        this.value = value;
    }

    /**
     * Retrieves the value of this literal expression.
     *
     * @return the literal value encapsulated by this expression
     */
    public @Nullable Object value() {
        return value;
    }

    @Override
    public String toSql(final Operation operation) {
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
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LiteralExpression.class.getSimpleName() + "[", "]")
                .add("value=" + value)
                .toString();
    }
}
