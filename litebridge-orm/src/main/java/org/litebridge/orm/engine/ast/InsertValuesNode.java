package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Represents the VALUES clause of an INSERT STATEMENT in the query AST.
 *
 * @param previous the previous node in the chain
 * @param values   values to insert
 */
public record InsertValuesNode(@Nullable QueryNode previous, @Nullable Object[] values) implements QueryNode {

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof InsertValuesNode(QueryNode previous1, Object[] values1))) return false;
        return Objects.equals(previous, previous1) && Objects.deepEquals(values, values1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous, values.length);
    }
}
