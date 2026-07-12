package org.litebridge.orm.api.dto.update;

import org.litebridge.orm.api.update.UpdateSetStep;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.meta.QueryField;

public interface DtoUpdateStart<DTO> {

    UpdateSetStep<DtoUpdateStep<DTO>> set(final String field);

    UpdateSetStep<DtoUpdateStep<DTO>> set(final ColumnExpressionSpec field);

    UpdateSetStep<DtoUpdateStep<DTO>> set(final QueryField field);

}
