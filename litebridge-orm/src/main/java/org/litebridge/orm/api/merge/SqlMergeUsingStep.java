package org.litebridge.orm.api.merge;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.SelectApi;
import org.litebridge.orm.api.select.SelectApiImpl;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.UsingNode;

import java.util.Objects;
import java.util.function.Function;

/**
 * Step to specify the table to use for a {@code USING} clause in a {@code MERGE} statement.
 */
public final class SqlMergeUsingStep extends MergeUsingStep<Row, SqlMergeUpdateStep, MergeInsertStep> {

    /**
     * Creates a new {@code SqlMergeUsingStep} instance.
     *
     * @param targetTable       the merge target table
     * @param litebridgeContext the Litebridge context
     */
    public SqlMergeUsingStep(final String targetTable, final LitebridgeContext litebridgeContext) {
        super(targetTable, litebridgeContext);
    }

    /**
     * Sets the {@code USING} table for the merge operation.
     *
     * @param usingTableName name of the table to use for the {@code USING} clause
     * @return step to specify the {@code ON} condition
     */
    public MergeOnStep<Row, SqlMergeUpdateStep, MergeInsertStep> using(final String usingTableName) {
        return new MergeOnStep<>(usingTableName, mergeNode, litebridgeContext);
    }

    /**
     * Specifies a subquery to use as the merge source.
     *
     * @param subselect function building the subquery
     * @return the merge ON condition clause terminal
     */
    public MergeOnStep<Row, SqlMergeUpdateStep, MergeInsertStep>  using(final Function<SelectApi, SelectTerminal<?>> subselect) {
        final SelectTerminal<?> selectTerminal = subselect.apply(new SelectApiImpl(litebridgeContext));
        final QueryNode subselectNode = Objects.requireNonNull(SelectTerminalInspector.getNode(selectTerminal));
        return new MergeOnStep<>(subselectNode, mergeNode, litebridgeContext);
    }
}
