package org.litebridge.orm.api.delete;

import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Entry step for constructing a {@code DELETE} statement.
 *
 * @param <DTO>  the mapped DTO/entity type or {@link org.litebridge.db.spi.Row}
 * @param <WCC>  the WHERE condition clause type
 * @param <WCCT> the WHERE condition clause terminal type
 */
public sealed interface DeleteStart<DTO,
        WCC extends DeleteWhereConditionClause<DTO, WCC, WCCT>,
        WCCT extends DeleteWhereConditionClauseTerminal<DTO, WCC, WCCT>>

        permits DtoDeleteStart, SqlDeleteStart {

    /**
     * Starts a WHERE clause with a column or field name.
     *
     * @param column the column or field name
     * @return step to specify the condition operator and value
     */
    DeleteWhereConditionClause<DTO, WCC, WCCT> where(final String column);

    /**
     * Starts a WHERE clause with an expression.
     *
     * @param expression the expression specification
     * @return step to specify the condition operator and value
     */
    DeleteWhereConditionClause<DTO, WCC, WCCT> where(final ExpressionSpec expression);

}
