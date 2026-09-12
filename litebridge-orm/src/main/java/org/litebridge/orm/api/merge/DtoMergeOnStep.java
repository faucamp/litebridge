package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Column;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.expression.select.SelectFieldSpec;
import org.litebridge.tracking.FieldAccessor;

/**
 * DTO-mode merge step for setting up the {@code MERGE INTO ... USING ... ON} condition.
 *
 * @param <DTO> the type of the DTO
 */
public final class DtoMergeOnStep<DTO> extends MergeOnStep<DTO, DtoMergeUpdateStep<DTO>, DtoMergeInsertStep> {

    public DtoMergeOnStep(final Class<?> usingDtoClass, final MergeNode mergeNode, final LitebridgeContext litebridgeContext) {
        super(usingDtoClass, mergeNode, litebridgeContext);
    }

    /**
     * Creates a {@code MERGE INTO ... USING ... ON} condition targeting the specified DTO/entity field.
     *
     * @param field the LHS mapped DTO field of the {@code ON} condition
     * @return the next step in the update operation: setting the value of the target field
     */
    @Override
    public MergeConditionClause<DTO, DtoMergeUpdateStep<DTO>, MergeOnConditionClauseTerminal<DTO, DtoMergeUpdateStep<DTO>, DtoMergeInsertStep>> on(final String field) {
        final FieldAccessor fieldAccessor = litebridgeContext.classFieldAccessorCache().fieldAccessor(usingDtoClass, field);
        final Column column = litebridgeContext.tableRegistry().getOrmTableOrThrow(usingDtoClass).columnMetaDataForField(field).toColumn();
        return on(new SelectFieldSpec(fieldAccessor, column));
    }
}
