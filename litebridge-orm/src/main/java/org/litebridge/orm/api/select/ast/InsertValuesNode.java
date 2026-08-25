package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Represents the VALUES clause of an INSERT STATEMENT in the query AST.
 *
 * @param previous the previous node in the chain
 * @param values   values to insert
 */
public record InsertValuesNode(@Nullable QueryNode previous, Object @Nullable [] values) implements QueryNode {

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final InsertValuesNode that)) return false;
        return Objects.equals(previous, that.previous) && Objects.deepEquals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous, values != null ? values.length : 0);
    }
}
