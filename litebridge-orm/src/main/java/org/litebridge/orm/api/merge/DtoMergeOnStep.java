package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.ast.MergeNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.select.SelectFieldSpec;
import org.litebridge.tracking.FieldAccessor;

public final class DtoMergeOnStep<DTO> extends MergeOnStep<DTO, DtoMergeUpdateStep<DTO>> {

    private final Class<?> dtoClass;

    public DtoMergeOnStep(final MergeNode mergeNode, final Table sourceTable, final LitebridgeContext litebridgeContext) {
        super(mergeNode, sourceTable, litebridgeContext);
        this.dtoClass = mergeNode.dtoClass();
    }

    @Override
    public MergeConditionClause<DTO, DtoMergeUpdateStep<DTO>, MergeOnConditionClauseTerminal<DTO, DtoMergeUpdateStep<DTO>>> on(final String field) {
        final FieldAccessor fieldAccessor = litebridgeContext.classFieldAccessorCache().fieldAccessor(dtoClass, field);
        final Column column = litebridgeContext.tableRegistry().getTableOrThrow(dtoClass).getColumnForFieldName(field).toColumn();
        return on(new SelectFieldSpec(fieldAccessor, column));
    }

    Class<?> dtoClass() {
        return dtoClass;
    }
}
