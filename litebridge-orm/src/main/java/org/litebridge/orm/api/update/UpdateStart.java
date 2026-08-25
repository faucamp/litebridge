package org.litebridge.orm.api.update;

import org.litebridge.orm.expression.ExpressionSpec;

public sealed interface UpdateStart<DTO,
        US extends UpdateStep<DTO, WCC, WCCT>,
        WCC extends UpdateWhereConditionClause<DTO, WCC, WCCT>,
        WCCT extends UpdateWhereConditionClauseTerminal<DTO, WCC, WCCT>>

        permits DtoUpdateStart, SqlUpdateStart, UpdateStep {

    UpdateSetStep<DTO, US, WCC, WCCT> set(final String column);

    UpdateSetStep<DTO, US, WCC, WCCT> set(final ExpressionSpec expression);

}
