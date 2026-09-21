package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a USING clause in a MERGE statement in the query AST.
 *
 * @param previous The root {@link MergeNode} for the MERGE INTO statement
 * @param table    The name of the table to merge into
 * @param dtoClass The DTO class to merge into
 * @param on       AST nodes representing the condition(s) to use for matching rows
 */
public record UsingNode(MergeNode previous,
                        @Nullable String table,
                        @Nullable Class<?> dtoClass,
                        @Nullable QueryNode query,
                        @Nullable String alias,
                        QueryNode on) implements QueryNode {

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final UsingNode usingNode)) return false;
        return Objects.equals(table, usingNode.table) && Objects.equals(alias, usingNode.alias) && Objects.equals(on, usingNode.on) && Objects.equals(query, usingNode.query) && Objects.equals(dtoClass, usingNode.dtoClass) && Objects.equals(previous, usingNode.previous);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous, table, dtoClass, query, alias, on);
    }
}
