package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;

import java.util.Objects;

/**
 * Represents a SET clause in an UPDATE statement in the query AST.
 *
 * @param previous  the previous node in the chain
 * @param column    the column to update
 * @param value     the value to set (can be a raw value or a {@link org.litebridge.db.spi.math.MathOperation})
 * @param bindValue whether the value should be bound as a parameter
 */
public record SetNode(@Nullable QueryNode previous, Column column, @Nullable Object value, boolean bindValue) implements QueryNode {

    public SetNode(@Nullable QueryNode previous, Column column, Object value) {
        this(previous, column, value, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SetNode setNode)) return false;
        return bindValue == setNode.bindValue && Objects.equals(previous, setNode.previous) && Objects.equals(column, setNode.column) && Objects.equals(structuralValue(), setNode.structuralValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous, column, structuralValue(), bindValue);
    }

    private @Nullable Object structuralValue() {
        return bindValue ? 1 : value;
    }
}
