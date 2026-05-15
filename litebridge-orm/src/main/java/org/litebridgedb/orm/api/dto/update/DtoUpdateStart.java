package org.litebridgedb.orm.api.dto.update;

import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.api.update.UpdateSetStep;

public interface DtoUpdateStart<DTO> {

    UpdateSetStep<DtoUpdateStep<DTO>> set(final String field);

    UpdateSetStep<DtoUpdateStep<DTO>> set(final FieldColumnSpec field);

}
