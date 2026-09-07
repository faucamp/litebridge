package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;

/**
 * Represents a MERGE INTO statement in the query AST.
 * <p>
 * This is a root node.
 *
 * @param table    the table to update
 * @param dtoClass class of the DTO to update
 */
public record MergeNode(@Nullable String table, @Nullable Class<?> dtoClass) implements QueryNode {

    @Override
    public @Nullable QueryNode previous() {
        return null;
    }
}
