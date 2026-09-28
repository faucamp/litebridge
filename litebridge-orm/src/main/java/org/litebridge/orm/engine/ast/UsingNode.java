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
        if (!(o instanceof UsingNode(
                MergeNode previous1, String table1, Class<?> aClass, QueryNode query1, String alias1, QueryNode on1
        ))) return false;
        return Objects.equals(table, table1) && Objects.equals(alias, alias1) && Objects.equals(on, on1) && Objects.equals(query, query1) && Objects.equals(dtoClass, aClass) && Objects.equals(previous, previous1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous, table, dtoClass, query, alias, on);
    }
}
