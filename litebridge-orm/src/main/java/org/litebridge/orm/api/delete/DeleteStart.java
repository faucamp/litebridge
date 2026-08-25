package org.litebridge.orm.api.delete;

import org.litebridge.orm.expression.ExpressionSpec;

public sealed interface DeleteStart<DTO,
        WCC extends DeleteWhereConditionClause<DTO, WCC, WCCT>,
        WCCT extends DeleteWhereConditionClauseTerminal<DTO, WCC, WCCT>>

        permits DtoDeleteStart, SqlDeleteStart {

    DeleteWhereConditionClause<DTO, WCC, WCCT> where(final String column);

    DeleteWhereConditionClause<DTO, WCC, WCCT> where(final ExpressionSpec expression);

}
