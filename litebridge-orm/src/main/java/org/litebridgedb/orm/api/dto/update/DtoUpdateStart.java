package org.litebridgedb.orm.api.dto.update;

import org.litebridgedb.orm.api.update.UpdateSetStep;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;

public interface DtoUpdateStart<DTO> {

    UpdateSetStep<DtoUpdateStep<DTO>> set(final String field);

    UpdateSetStep<DtoUpdateStep<DTO>> set(final ColumnExpressionSpec field);

}
