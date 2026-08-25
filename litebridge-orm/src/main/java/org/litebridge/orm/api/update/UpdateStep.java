package org.litebridge.orm.api.update;

import org.litebridge.orm.expression.ExpressionSpec;

public sealed interface UpdateStep<DTO,
        WCC extends UpdateWhereConditionClause<DTO, WCC, WCCT>,
        WCCT extends UpdateWhereConditionClauseTerminal<DTO, WCC, WCCT>>

        extends UpdateStart, UpdateQuery
        permits DtoUpdateStep, SqlUpdateStep {

    UpdateWhereConditionClause<DTO, WCC, WCCT> where(final String column);

    UpdateWhereConditionClause<DTO, WCC, WCCT> where(final ExpressionSpec expression);

}
