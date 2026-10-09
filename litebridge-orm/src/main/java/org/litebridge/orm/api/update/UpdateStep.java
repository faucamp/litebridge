package org.litebridge.orm.api.update;

import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.ExistsExpressionSpec;

/**
 * Step in an {@code UPDATE} statement allowing additional SET assignments or transitioning to a WHERE clause.
 *
 * @param <DTO>  the mapped DTO/entity type or {@link org.litebridge.db.spi.Row}
 * @param <WCC>  the WHERE condition clause type
 * @param <WCCT> the WHERE condition clause terminal type
 */
public sealed interface UpdateStep<DTO,
        SELF extends UpdateStep<DTO, SELF, USS, WCC, WCCT, QCB>,
        USS extends UpdateSetStep<DTO, SELF, USS, WCC, WCCT, QCB>,
        WCC extends UpdateWhereConditionClause<DTO, WCC, WCCT, QCB>,
        WCCT extends UpdateWhereConditionClauseTerminal<DTO, WCC, WCCT, QCB>,
        QCB extends QueryConditionBuilder<DTO, ?, ?, ?, QCB>>

        extends UpdateStart<DTO, SELF, USS, WCC, WCCT, QCB>, UpdateQuery
        permits DtoUpdateStep, SqlUpdateStep {

    /**
     * Starts a WHERE clause with a column or field name.
     *
     * @param column the column or field name
     * @return step to specify the condition operator and value
     */
    WCC where(final String column);

    /**
     * Starts a WHERE clause with an expression.
     *
     * @param expression the expression specification
     * @return step to specify the condition operator and value
     */
    WCC where(final ExpressionSpec expression);

    /**
     * Starts a WHERE clause with an EXISTS expression.
     *
     * @param existsExpression the EXISTS expression specification
     * @return step to specify the condition operator and value
     */
    WCCT where(final ExistsExpressionSpec existsExpression);

}
