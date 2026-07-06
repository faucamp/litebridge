package org.litebridgedb.orm.api.dto.update;

import org.litebridgedb.orm.api.update.UpdateSetStep;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.meta.QueryField;

public interface DtoUpdateStart<DTO> {

    UpdateSetStep<DtoUpdateStep<DTO>> set(final String field);

    UpdateSetStep<DtoUpdateStep<DTO>> set(final ColumnExpressionSpec field);

    UpdateSetStep<DtoUpdateStep<DTO>> set(final QueryField field);

}
