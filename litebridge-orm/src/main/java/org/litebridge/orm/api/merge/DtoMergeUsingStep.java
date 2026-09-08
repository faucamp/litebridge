package org.litebridge.orm.api.merge;

import org.litebridge.orm.engine.LitebridgeContext;

/**
 * Step to specify the DTO/entity class to use for a {@code USING} clause in a {@code MERGE} statement.
 */
public final class DtoMergeUsingStep<DTO> extends MergeUsingStep<DTO, DtoMergeUpdateStep<DTO>, DtoMergeInsertStep> {

    /**
     * Creates a new {@code DtoMergeUsingStep} instance.
     *
     * @param dtoClass          the DTO/entity class to use for the {@code USING} clause
     * @param litebridgeContext the Litebridge context
     */
    public DtoMergeUsingStep(final Class<DTO> dtoClass, final LitebridgeContext litebridgeContext) {
        super(dtoClass, litebridgeContext);
    }

    /**
     * Specifies the DTO/entity class to use for the {@code USING} clause.
     *
     * @param dtoClass the DTO/entity class to use for the {@code USING} clause
     * @return the next step in the merge operation
     */
    public DtoMergeOnStep<DTO> using(final Class<?> dtoClass) {
        return new DtoMergeOnStep<>(dtoClass, mergeNode, litebridgeContext);
    }
}
