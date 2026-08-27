package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a SELECT clause in the query AST.
 *
 * @param expressions the expressions to select
 * @param resultType  the target result type, if overridden
 */
public record SelectNode(@Nullable String table,
                         @Nullable Class<?> dtoClass,
                         String @Nullable [] columns,
                         ExpressionSpec @Nullable [] expressions,
                         @Nullable Class<?> resultType) implements QueryNode {

    public boolean isSelectAll() {
        return columns == null && expressions == null;
    }

    @Override
    public @Nullable QueryNode previous() {
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final SelectNode that)) return false;
        return Objects.equals(table, that.table) && Objects.equals(dtoClass, that.dtoClass) && Objects.equals(resultType, that.resultType) && Objects.deepEquals(columns, that.columns) && Objects.deepEquals(expressions, that.expressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, dtoClass, Arrays.hashCode(columns), Arrays.hashCode(expressions), resultType);
    }
}
