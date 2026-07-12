package org.litebridge.orm.api.dto.delete;

import org.litebridge.orm.api.delete.DeleteQuery;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;

public sealed interface DtoDeleteWhereClause<DTO> extends DeleteQuery permits DtoDeletor {

    DtoDeleteWhereConditionClause<DTO> where(final String field);

    DtoDeleteWhereConditionClause<DTO> where(final ExpressionSpec expression);
}
