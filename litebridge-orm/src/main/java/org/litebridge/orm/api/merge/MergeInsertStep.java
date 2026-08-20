package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.insert.InsertValuesStep;
import org.litebridge.orm.api.select.ast.InsertNode;
import org.litebridge.orm.engine.LitebridgeContext;

public class MergeInsertStep {

    private final Table table;
    private final LitebridgeContext litebridgeContext;

    public MergeInsertStep(final Table table, final LitebridgeContext litebridgeContext) {
        this.table = table;
        this.litebridgeContext = litebridgeContext;
    }

    public MergeTerminal insert(final Object dto) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public InsertValuesStep insert(final String... columns) {
        final InsertNode insertNode = new InsertNode(null, table, columns);
        return new InsertValuesStep(insertNode, litebridgeContext);
    }
}
