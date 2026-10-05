package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a VALUES clause of an INSERT STATEMENT in the query AST, specifically for extracting and inserting values from a DTO instance.
 *
 * @param previous the previous node in the chain
 * @param dto   the DTO instance to extract values to insert from
 */
public record InsertDtoValuesNode(@Nullable QueryNode previous, Object dto) implements QueryNode {

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof InsertDtoValuesNode(QueryNode previous1, Object dto1))) return false;
        return Objects.equals(previous, previous1) && Objects.equals(dto, dto1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous);
    }
}
