package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Column;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.select.SelectFieldSpec;
import org.litebridge.tracking.FieldAccessor;

public final class DtoMergeOnStep<DTO> extends MergeOnStep<DTO, DtoMergeUpdateStep<DTO>> {

    public DtoMergeOnStep(final Class<?> usingDtoClass, final MergeNode mergeNode, final LitebridgeContext litebridgeContext) {
        super(usingDtoClass, mergeNode, litebridgeContext);
    }

    @Override
    public MergeConditionClause<DTO, DtoMergeUpdateStep<DTO>, MergeOnConditionClauseTerminal<DTO, DtoMergeUpdateStep<DTO>>> on(final String field) {
        final FieldAccessor fieldAccessor = litebridgeContext.classFieldAccessorCache().fieldAccessor(usingDtoClass, field);
        final Column column = litebridgeContext.tableRegistry().getOrmTableOrThrow(usingDtoClass).columnMetaDataForField(field).toColumn();
        return on(new SelectFieldSpec(fieldAccessor, column));
    }
}
