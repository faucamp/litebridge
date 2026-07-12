package org.litebridge.orm.api.dto.delete;

import org.litebridge.orm.api.delete.DeleteQuery;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * A WHERE clause for DTO-based delete operations.
 *
 * @param <DTO> the type of the DTO
 */
public sealed interface DtoDeleteWhereClause<DTO> extends DeleteQuery permits DtoDeletor {

    /**
     * Creates a condition based on a field name.
     *
     * @param field the field name
     * @return a condition clause
     */
    DtoDeleteWhereConditionClause<DTO> where(final String field);

    /**
     * Creates a condition based on an expression.
     *
     * @param expression the expression
     * @return a condition clause
     */
    DtoDeleteWhereConditionClause<DTO> where(final ExpressionSpec expression);
}
