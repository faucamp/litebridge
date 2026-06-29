package org.litebridgedb.orm.api.dto.update;

import org.litebridgedb.orm.api.update.UpdateQuery;
import org.litebridgedb.orm.api.update.UpdateStep;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;

public sealed interface DtoUpdateStep<DTO> extends DtoUpdateStart<DTO>, UpdateStep, UpdateQuery permits DtoUpdater {

    DtoUpdateWhereConditionClause<DTO> where(final String field);

    DtoUpdateWhereConditionClause<DTO> where(final ColumnExpressionSpec field);
}
