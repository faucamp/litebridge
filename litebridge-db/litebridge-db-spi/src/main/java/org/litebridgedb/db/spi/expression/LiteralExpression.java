package org.litebridgedb.db.spi.expression;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Operation;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * An encapsulated literal rhs in a query expression.
 */
public class LiteralExpression implements SelectExpression {

    private final @Nullable Object value;

    /**
     * Constructs a new {@code LiteralExpression} with the given rhs.
     *
     * @param value the literal rhs to be represented
     */
    public LiteralExpression(final @Nullable Object value) {
        this.value = value;
    }

    /**
     * Retrieves the rhs of this literal expression.
     *
     * @return the literal rhs encapsulated by this expression
     */
    public @Nullable Object value() {
        return value;
    }

    @Override
    public String toSql(final Operation operation) {
        return value != null ? value.toString() : "NULL";
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
                .add("rhs=" + value)
                .toString();
    }
}
