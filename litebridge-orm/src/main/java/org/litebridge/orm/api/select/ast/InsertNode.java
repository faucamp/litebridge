package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Represents an INSERT statement in the query AST.
 *
 * @param previous the previous node in the chain
 * @param table    the table to insert into
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
}
