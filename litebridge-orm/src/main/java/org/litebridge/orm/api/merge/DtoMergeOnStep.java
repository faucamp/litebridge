package org.litebridge.orm.api.merge;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.expression.select.ValuesSpec;

import java.util.Objects;

/**
 * DTO-mode merge step for setting up the {@code MERGE INTO ... USING ... ON} condition.
 *
 * @param <DTO> the type of the DTO
 */
public final class DtoMergeOnStep<DTO> extends MergeOnStep<DTO, DtoMergeUpdateStep<DTO>, DtoMergeInsertStep> {

    /**
     * Creates a new {@code DtoMergeOnStep} instance using an entity/mapped DTO class.
     *
     * @param usingDtoClass     the DTO/entity class used in the {@code USING} clause
     * @param mergeNode         the current merge node
     * @param litebridgeContext the Litebridge context
     */
    public DtoMergeOnStep(final Class<?> usingDtoClass,
                          final @Nullable String usingAlias,
                          final MergeNode mergeNode,
                          final LitebridgeContext litebridgeContext) {
        super(usingDtoClass, usingAlias, mergeNode, litebridgeContext);
    }

    /**
     * Creates a new {@code DtoMergeOnStep} instance using a subquery.
     *
     * @param subselectNode     the subquery node
     * @param usingAlias        the alias for the subquery
     * @param mergeNode         the root merge node
     * @param litebridgeContext the Litebridge context
     */
    public DtoMergeOnStep(final QueryNode subselectNode,
                          final @Nullable String usingAlias,
                          final MergeNode mergeNode, final
                          LitebridgeContext litebridgeContext) {
        super(subselectNode, usingAlias, mergeNode, litebridgeContext);
    }

    /**
     * Creates a new {@code DtoMergeOnStep} instance using VALUES.
     *
     * @param valuesSpec        Values from target specification
     * @param mergeNode         the root merge node
     * @param litebridgeContext the Litebridge context
     */
    public DtoMergeOnStep(final ValuesSpec valuesSpec,
                          final MergeNode mergeNode,
                          final LitebridgeContext litebridgeContext) {
        super(valuesSpec, mergeNode, litebridgeContext);
    }

    /**
     * Creates a {@code MERGE INTO ... USING ... ON} condition targeting the specified DTO/entity field.
     *
     * @param field the LHS mapped DTO field of the {@code ON} condition
     * @return the next step in the update operation: setting the value of the target field
     */
    @Override
    public MergeConditionClause<DTO, DtoMergeUpdateStep<DTO>, MergeOnConditionClauseTerminal<DTO, DtoMergeUpdateStep<DTO>, DtoMergeInsertStep>> on(final String field) {
        final Column column = litebridgeContext.tableRegistry().getOrmTableOrThrow(Objects.requireNonNull(usingDtoClass)).columnMetaDataForField(field).column();
        return on(new SelectColumnSpec(column));
    }
}
