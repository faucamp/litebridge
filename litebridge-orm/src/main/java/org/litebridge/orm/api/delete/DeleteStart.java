package org.litebridge.orm.api.delete;

import org.litebridge.orm.expression.ExpressionSpec;

public interface DeleteStart<DTO,
        WCC extends DeleteWhereConditionClause<DTO, WCC, WCCT>,
        WCCT extends DeleteWhereConditionClauseTerminal<DTO, WCC, WCCT>> {

    DeleteWhereConditionClause<DTO, WCC, WCCT> where(final String column);

    DeleteWhereConditionClause<DTO, WCC, WCCT> where(final ExpressionSpec expression);

}
