package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.engine.LitebridgeContext;

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
}
