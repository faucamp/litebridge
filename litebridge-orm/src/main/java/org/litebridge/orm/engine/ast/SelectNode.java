package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a SELECT clause in the query AST.
 * <p>
 * This is a root node.
 *
 * @param table           name of the table to select from
 * @param dtoClass        class of the DTO to select from
 * @param contextDtoClass The parent/context DTO class for determining the correct {@code dtoClass} table mapping (for shared DTOs mapped to multiple distinct tables)
 * @param fromQueryNode   the query node representing the subquery in the FROM clause
 * @param columns         names of the columns to select
 * @param expressions     the expressions to select
 * @param resultTypes     the target result types, if overridden
 */
public record SelectNode(@Nullable String table,
                         @Nullable Class<?> dtoClass,
                         @Nullable Class<?> contextDtoClass,
                         @Nullable QueryNode fromQueryNode,
                         @Nullable String alias,
                         String @Nullable [] columns,
                         ExpressionSpec @Nullable [] expressions,
                         @Nullable Class<?> @Nullable [] resultTypes) implements QueryNode {

    public SelectNode(final String table,
                      final @Nullable String alias,
                      final String @Nullable [] columns,
                      final ExpressionSpec @Nullable [] expressions,
                      final @Nullable Class<?> @Nullable [] resultTypes) {
        this(table, null, null, null, alias, columns, expressions, resultTypes);
    }

    public SelectNode(final Class<?> dtoClass,
                      final @Nullable Class<?> contextDtoClass,
                      final @Nullable String alias,
                      final String @Nullable [] columns,
                      final ExpressionSpec @Nullable [] expressions,
                      final @Nullable Class<?> @Nullable [] resultTypes) {
        this(null, dtoClass, contextDtoClass, null, alias, columns, expressions, resultTypes);
    }

    public SelectNode(final @Nullable QueryNode fromQueryNode,
                      final @Nullable String alias,
                      final String @Nullable [] columns,
                      final ExpressionSpec @Nullable [] expressions,
                      final @Nullable Class<?> @Nullable [] resultTypes) {
        this(null, null, null, fromQueryNode, alias, columns, expressions, resultTypes);
    }

    public SelectNode(final String @Nullable [] columns,
                      final ExpressionSpec @Nullable [] expressions,
                      final @Nullable Class<?> @Nullable [] resultTypes) {
        this(null, null, null, null, null, columns, expressions, resultTypes);
    }

    /**
     * Checks whether this node represents selecting all columns/fields.
     *
     * @return {@code true} if all columns/fields are selected; {@code false} otherwise
     */
    public boolean isSelectAll() {
        return columns == null && expressions == null;
    }

    @Override
    public @Nullable QueryNode previous() {
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof SelectNode(
                String table1, Class<?> thatDtoClass,
                Class<?> thatContextDtoClass,
                QueryNode thatFromQueryNode,
                String thatAlias,
                String[] thatColumns,
                ExpressionSpec[] thatExpressions,
                Class<?>[] thatResultTypes
        ))) return false;
        return Objects.equals(table, table1)
                && Objects.equals(dtoClass, thatDtoClass)
                && Objects.equals(contextDtoClass, thatContextDtoClass)
                && Objects.equals(fromQueryNode, thatFromQueryNode)
                && Objects.equals(alias, thatAlias)
                && Arrays.deepEquals(resultTypes, thatResultTypes)
                && Arrays.deepEquals(columns, thatColumns)
                && Arrays.deepEquals(expressions, thatExpressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table,
                dtoClass,
                contextDtoClass,
                fromQueryNode,
                alias,
                Arrays.hashCode(columns),
                Arrays.deepHashCode(expressions),
                Arrays.hashCode(resultTypes));
    }
}
