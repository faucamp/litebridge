package org.litebridge.orm.api.dto.update;

import org.litebridge.orm.api.update.UpdateQuery;
import org.litebridge.orm.api.update.UpdateStep;
import org.litebridge.orm.expression.ExpressionSpec;

public sealed interface DtoUpdateStep<DTO> extends DtoUpdateStart<DTO>, UpdateStep, UpdateQuery permits DtoUpdater {

    DtoUpdateWhereConditionClause<DTO> where(final String field);

    DtoUpdateWhereConditionClause<DTO> where(final ExpressionSpec expression);
}
