package org.litebridge.orm.api.merge;

import org.litebridge.orm.engine.LitebridgeContext;

public final class DtoMergeUsingStep<DTO> extends MergeUsingStep<DTO, DtoMergeUpdateStep<DTO>> {

    public DtoMergeUsingStep(final Class<DTO> dtoClass, final LitebridgeContext litebridgeContext) {
        super(dtoClass, litebridgeContext);
    }

    public DtoMergeOnStep<DTO> using(final Class<?> dtoClass) {
        return new DtoMergeOnStep<>(dtoClass, mergeNode, litebridgeContext);
    }
}
