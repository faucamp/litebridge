package org.litebridge.orm.api.dto.update;

import org.litebridge.orm.api.spec.FieldColumnSpec;
import org.litebridge.orm.api.update.UpdateSetStep;

public interface DtoUpdateStart<DTO> {

    UpdateSetStep<DtoUpdateStep<DTO>> set(final String field);

    UpdateSetStep<DtoUpdateStep<DTO>> set(final FieldColumnSpec field);

}
