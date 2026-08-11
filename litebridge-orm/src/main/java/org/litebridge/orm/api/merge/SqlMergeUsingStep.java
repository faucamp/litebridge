package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.engine.LitebridgeContext;

public final class SqlMergeUsingStep extends MergeUsingStep<Row, SqlMergeUpdateStep> {

    public SqlMergeUsingStep(final Table destinationTable, final LitebridgeContext litebridgeContext) {
        super(Row.class, destinationTable, litebridgeContext);
    }

    public MergeOnStep<Row, SqlMergeUpdateStep> using(final String sourceTableName) {
        return new MergeOnStep<>(mergeNode, new Table(sourceTableName), litebridgeContext);
    }
}
