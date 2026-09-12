package org.litebridge.orm.api.update;

import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Step in an {@code UPDATE} statement allowing additional SET assignments or transitioning to a WHERE clause.
 *
 * @param <DTO>  the mapped DTO/entity type or {@link org.litebridge.db.spi.Row}
 * @param <WCC>  the WHERE condition clause type
 * @param <WCCT> the WHERE condition clause terminal type
 */
public sealed interface UpdateStep<DTO,
        WCC extends UpdateWhereConditionClause<DTO, WCC, WCCT>,
        WCCT extends UpdateWhereConditionClauseTerminal<DTO, WCC, WCCT>>

        extends UpdateStart, UpdateQuery
        permits DtoUpdateStep, SqlUpdateStep {

    /**
     * Starts a WHERE clause with a column or field name.
     *
     * @param column the column or field name
     * @return step to specify the condition operator and value
     */
    UpdateWhereConditionClause<DTO, WCC, WCCT> where(final String column);

    /**
     * Starts a WHERE clause with an expression.
     *
     * @param expression the expression specification
     * @return step to specify the condition operator and value
     */
    UpdateWhereConditionClause<DTO, WCC, WCCT> where(final ExpressionSpec expression);

}
