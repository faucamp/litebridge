package org.litebridgedb.orm.api.dto.delete;

import org.litebridgedb.orm.api.delete.DeleteQuery;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;

public sealed interface DtoDeleteWhereClause<DTO> extends DeleteQuery permits DtoDeletor {

    DtoDeleteWhereConditionClause<DTO> where(final String field);

    DtoDeleteWhereConditionClause<DTO> where(final ColumnExpressionSpec field);
}
