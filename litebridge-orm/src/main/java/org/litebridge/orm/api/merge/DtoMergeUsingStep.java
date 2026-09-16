package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.SelectApi;
import org.litebridge.orm.api.select.SelectApiImpl;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.QueryNode;

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

    /**
     * Specifies a subquery to use as the merge source.
     *
     * @param subselect function building the subquery
     * @return the merge ON condition clause terminal
     */
    public DtoMergeOnStep<DTO> using(final Function<SelectApi, SelectTerminal<?>> subselect) {
        final SelectTerminal<?> selectTerminal = subselect.apply(new SelectApiImpl(litebridgeContext));
        final QueryNode subselectNode = Objects.requireNonNull(SelectTerminalInspector.getNode(selectTerminal));
        return new DtoMergeOnStep<>(subselectNode, mergeNode, litebridgeContext);
    }
}
