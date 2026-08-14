package org.litebridge.orm.api.insert;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.ast.InsertNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;

public final class InsertIntoStep {

    private final @Nullable QueryNode node;
    private final LitebridgeContext litebridgeContext;
    private final Table table;

    public InsertIntoStep(final Table table,
                          final @Nullable QueryNode node,
                          final LitebridgeContext litebridgeContext) {
        this.node = node;
        this.litebridgeContext = litebridgeContext;
        this.table = table;
    }

    public InsertValuesStep into(final String... columns) {
        final InsertNode insertNode = new InsertNode(node, table, columns);
        return new InsertValuesStep(insertNode, litebridgeContext);
    }
}
