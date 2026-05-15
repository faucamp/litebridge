package org.litebridgedb.orm.api.dto.update;

import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.api.update.UpdateQuery;
import org.litebridgedb.orm.api.update.UpdateStep;

public sealed interface DtoUpdateStep<DTO> extends DtoUpdateStart<DTO>, UpdateStep, UpdateQuery permits DtoUpdater {

    DtoUpdateWhereConditionClause<DTO> where(final String field);

    DtoUpdateWhereConditionClause<DTO> where(final FieldColumnSpec field);
}
