package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.expression.select.ValuesSpec;

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
                        @Nullable ValuesSpec values,
                        @Nullable String alias,
                        QueryNode on) implements QueryNode {

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof UsingNode(
                MergeNode oPrevious,
                String oTable,
                Class<?> oDtoClass,
                QueryNode oQuery,
                ValuesSpec oValues,
                String oAlias, QueryNode oOn
        ))) return false;
        return Objects.equals(table, oTable) && Objects.equals(alias, oAlias)
                && Objects.equals(on, oOn)
                && Objects.equals(query, oQuery)
                && Objects.equals(dtoClass, oDtoClass)
                && Objects.equals(values, oValues)
                && Objects.equals(previous, oPrevious);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous, table, dtoClass, query, values, alias, on);
    }
}
