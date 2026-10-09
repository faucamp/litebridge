package org.litebridge.orm.api.update;

import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Entry step for constructing an {@code UPDATE} statement.
 *
 * @param <DTO>  the mapped DTO/entity type or {@link org.litebridge.db.spi.Row}
 * @param <US>   the update step type
 * @param <WCC>  the WHERE condition clause type
 * @param <WCCT> the WHERE condition clause terminal type
 */
public sealed interface UpdateStart<DTO,
        US extends UpdateStep<DTO, US, USS, WCC, WCCT, QCB>,
        USS extends UpdateSetStep<DTO, US, USS, WCC, WCCT, QCB>,
        WCC extends UpdateWhereConditionClause<DTO, WCC, WCCT, QCB>,
        WCCT extends UpdateWhereConditionClauseTerminal<DTO, WCC, WCCT, QCB>,
        QCB extends QueryConditionBuilder<DTO, ?, ?, ?, QCB>>

        permits DtoUpdateStart, SqlUpdateStart, UpdateStep {

    /**
     * Starts a SET clause with a column or field name.
     *
     * @param column the column or field name
     * @return step to specify the value for the column
     */
    USS set(final String column);

    /**
     * Starts a SET clause with an expression.
     *
     * @param expression the expression specification
     * @return step to specify the value for the expression
     */
    USS set(final ExpressionSpec expression);
}
