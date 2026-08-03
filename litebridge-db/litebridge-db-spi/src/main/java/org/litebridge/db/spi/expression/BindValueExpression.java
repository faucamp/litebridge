package org.litebridge.db.spi.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * An encapsulated literal value in a query expression.
 */
public record BindValueExpression(int index, int size) implements SelectExpression {

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        if (size > 1) {
            final StringJoiner joiner = new StringJoiner(", ");

            for (int i = 0; i < size; i++) {
                joiner.add("?");
            }

            return joiner.toString();
        } else {
            return "?";
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final BindValueExpression that)) return false;
        return index == that.index;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(index);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BindValueExpression.class.getSimpleName() + "[", "]")
                .add("index=" + index)
                .toString();
    }
}
