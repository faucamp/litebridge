package org.litebridge.orm.api.dto.update;

import org.litebridge.orm.api.spec.FieldColumnSpec;
import org.litebridge.orm.api.update.UpdateQuery;
import org.litebridge.orm.api.update.UpdateStep;

public sealed interface DtoUpdateStep<DTO> extends DtoUpdateStart<DTO>, UpdateStep, UpdateQuery permits DtoUpdater {

    DtoUpdateWhereConditionClause<DTO> where(final String field);

    DtoUpdateWhereConditionClause<DTO> where(final FieldColumnSpec field);
}
