package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Table;
import org.litebridge.orm.engine.LitebridgeContext;

public final class DtoMergeUsingStep<DTO> extends MergeUsingStep<DTO, DtoMergeUpdateStep<DTO>> {

    public DtoMergeUsingStep(final Class<DTO> dtoClass, final LitebridgeContext litebridgeContext) {
        super(dtoClass,
                litebridgeContext.tableRegistry().getTableOrThrow(dtoClass).getMetaData().toTable(),
                litebridgeContext);
    }

    public DtoMergeOnStep<DTO> using(final Class<?> dtoClass) {
        final Table sourceTable = litebridgeContext.tableRegistry().getTableOrThrow(dtoClass).getMetaData().toTable();
        return new DtoMergeOnStep<>(mergeNode, sourceTable, litebridgeContext);
    }
}
