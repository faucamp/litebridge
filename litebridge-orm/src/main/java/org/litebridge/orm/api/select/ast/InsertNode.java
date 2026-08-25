package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents an INSERT statement in the query AST.
 *
 * @param table           name of the table to insert into
 * @param dtoClass        class of the DTO to insert
 * @param columns         names of the columns to insert into
 * @param expressionSpecs expressions to use instead of columns
 */
public record InsertNode(@Nullable String table,
                         @Nullable Class<?> dtoClass,
                         @Nullable String[] columns,
                         @Nullable ExpressionSpec[] expressionSpecs) implements QueryNode {

    public InsertNode(final @Nullable String table, final @Nullable Class<?> dtoClass, final String[] columns) {
        this(table, dtoClass, columns, null);
    }

    public InsertNode(final @Nullable String table, final @Nullable Class<?> dtoClass, final ExpressionSpec[] expressionSpecs) {
        this(table, dtoClass, null, expressionSpecs);
    }

    @Override
    public @Nullable QueryNode previous() {
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final InsertNode that)) return false;
        return Objects.equals(table, that.table) && Objects.deepEquals(columns, that.columns) && Objects.equals(dtoClass, that.dtoClass) && Objects.deepEquals(expressionSpecs, that.expressionSpecs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, dtoClass, Arrays.hashCode(columns), Arrays.hashCode(expressionSpecs));
    }
}
