package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Represents a JOIN clause metadata in the query AST.
 *
 * @param previous  the previous node in the chain
 * @param type      the join type (e.g., INNER, LEFT)
 * @param dtoClass  the DTO class to join (if DTO-based)
 * @param tableName the table name to join (if SQL-based)
 */
public record JoinNode(@Nullable QueryNode previous,
                       String type,
                       @Nullable Class<?> dtoClass,
                       @Nullable Class<?> sourceDtoClass,
                       @Nullable String tableName) implements QueryNode {

    public JoinNode(@Nullable QueryNode previous,
                    String type,
                    @Nullable Class<?> dtoClass,
                    @Nullable String tableName) {
        this(previous, type, dtoClass, null, tableName);
    }
}
