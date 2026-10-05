package org.litebridge.orm.api.merge;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.select.SelectApi;
import org.litebridge.orm.api.select.SelectApiImpl;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.select.DtoAliasSpec;
import org.litebridge.orm.expression.select.FromTargetSpec;
import org.litebridge.orm.expression.select.QueryAliasSpec;
import org.litebridge.orm.expression.select.ValuesSpec;

import java.util.Objects;
import java.util.function.Function;

/**
 * Step to specify the DTO/entity class to use for a {@code USING} clause in a {@code MERGE} statement.
 *
 * @param <DTO> the mapped DTO/entity type
 */
public final class DtoMergeUsingStep<DTO> extends MergeUsingStep<DTO, DtoMergeUpdateStep<DTO>, DtoMergeInsertStep> {

    /**
     * Creates a new {@code DtoMergeUsingStep} instance.
     *
     * @param dtoClass          the DTO/entity class to use for the {@code USING} clause
     * @param contextDtoClass   the parent context DTO/entity class to use for the {@code USING} clause
     * @param litebridgeContext the Litebridge context
     */
    public DtoMergeUsingStep(final Class<DTO> dtoClass, final @Nullable Class<?> contextDtoClass, final LitebridgeContext litebridgeContext) {
        super(dtoClass, contextDtoClass, litebridgeContext);
    }

    /**
     * Specifies the DTO/entity class to use for the {@code USING} clause.
     *
     * @param dtoClass the DTO/entity class to use for the {@code USING} clause
     * @return the next step in the merge operation
     */
    public DtoMergeOnStep<DTO> using(final Class<?> dtoClass) {
        return new DtoMergeOnStep<>(dtoClass, null, mergeNode, litebridgeContext);
    }

    /**
     * Specifies a subquery to use as the merge source.
     *
     * @param subselect function building the subquery
     * @return the merge ON condition clause terminal
     */
    public DtoMergeOnStep<DTO> using(final Function<SelectApi, SelectTerminal<?>> subselect) {
        return usingQueryImpl(subselect, null);
    }

    public DtoMergeOnStep<DTO> using(final FromTargetSpec fromTargetSpec) {
        return switch (fromTargetSpec) {
            case QueryAliasSpec queryAliasSpec -> usingQueryImpl(queryAliasSpec.query(), queryAliasSpec.alias());
            case DtoAliasSpec<?> dtoAliasSpec ->
                    new DtoMergeOnStep<>(dtoAliasSpec.dtoClass(), dtoAliasSpec.alias(), mergeNode, litebridgeContext);
            case ValuesSpec valuesSpec -> new DtoMergeOnStep<>(valuesSpec, mergeNode, litebridgeContext);
            default -> throw new IllegalArgumentException("Unsupported DTO-mode FromTargetSpec: " + fromTargetSpec);
        };
    }

    private DtoMergeOnStep<DTO> usingQueryImpl(final Function<SelectApi, SelectTerminal<?>> subselect, final @Nullable String alias) {
        final SelectTerminal<?> selectTerminal = subselect.apply(new SelectApiImpl(litebridgeContext));
        final QueryNode subselectNode = Objects.requireNonNull(SelectTerminalInspector.getNode(selectTerminal));
        return new DtoMergeOnStep<>(subselectNode, alias, mergeNode, litebridgeContext);
    }
}
