package org.litebridge.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Specification for a condition in a database query.
 * <p>
 * This is used in a SQL query WHERE clause, JOIN clause, etc.
 * <p>
 * A condition consists of a column, an operator, and an optional value. The operator
 * dictates how the column will be compared to the provided value.
 *
 * @param lhsColumn     the left-hand side column name
 * @param lhsExpression the left-hand side expression
 * @param operator      the condition operator
 * @param value         the raw right-hand side value
 */
public record ConditionSpec(@Nullable String lhsColumn,
                            @Nullable ExpressionSpec lhsExpression,
                            @Nullable Operator operator,
                            @Nullable Object value) {
}
