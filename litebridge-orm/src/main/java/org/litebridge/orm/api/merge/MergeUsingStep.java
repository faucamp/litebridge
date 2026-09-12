package org.litebridge.orm.api.merge;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngine;
import org.litebridge.orm.engine.ast.MergeNode;

import java.util.function.Function;

/**
 * Abstract base step for specifying the source in a {@code MERGE} statement.
 *
 * @param <DTO> the target DTO type or {@link org.litebridge.db.spi.Row}
 * @param <MUS> the merge update step type
 * @param <MIS> the merge insert step type
 */
public abstract sealed class MergeUsingStep<DTO, MUS extends MergeUpdateStep, MIS extends MergeInsertStep>
        permits DtoMergeUsingStep, SqlMergeUsingStep {

    /**
     * The current merge AST node.
     */
    protected final MergeNode mergeNode;
    /**
     * The Litebridge context.
     */
    protected final LitebridgeContext litebridgeContext;

    /**
     * Creates a new {@code MergeUsingStep} targeting a table.
     *
     * @param destinationTable  the destination table name
     * @param litebridgeContext the Litebridge context
     */
    protected MergeUsingStep(final String destinationTable, final LitebridgeContext litebridgeContext) {
        this.mergeNode = new MergeNode(destinationTable, null);
        this.litebridgeContext = litebridgeContext;
    }

    /**
     * Creates a new {@code MergeUsingStep} targeting a DTO class.
     *
     * @param dtoClass          the destination DTO class
     * @param litebridgeContext the Litebridge context
     */
    protected MergeUsingStep(final Class<DTO> dtoClass, final LitebridgeContext litebridgeContext) {
        this.mergeNode = new MergeNode(null, dtoClass);
        this.litebridgeContext = litebridgeContext;
    }

    /**
     * Specifies a subquery to use as the merge source.
     *
     * @param subselect function building the subquery
     * @return the merge ON condition clause terminal
     */
    public MergeOnConditionClauseTerminal<DTO, MUS, MIS> using(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
