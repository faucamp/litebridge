package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.engine.LitebridgeContext;

public final class SqlMergeUsingStep extends MergeUsingStep<Row, SqlMergeUpdateStep> {

    public SqlMergeUsingStep(final String destinationTable, final LitebridgeContext litebridgeContext) {
        super(destinationTable, litebridgeContext);
    }

    public MergeOnStep<Row, SqlMergeUpdateStep> using(final String usingTableName) {
        return new MergeOnStep<>(usingTableName, mergeNode, litebridgeContext);
    }
}
