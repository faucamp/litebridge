package org.litebridge.orm.api.merge;

import org.litebridge.orm.api.insert.InsertValuesStep;
import org.litebridge.orm.engine.ast.InsertNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

public class MergeInsertStep {

    private final String table;
    private final LitebridgeContext litebridgeContext;

    public MergeInsertStep(final String table, final LitebridgeContext litebridgeContext) {
        this.table = table;
        this.litebridgeContext = litebridgeContext;
    }

    public MergeTerminal insert(final Object dto) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public InsertValuesStep insert(final ExpressionSpec... expressions) {
        final InsertNode insertNode = new InsertNode(table, null, expressions);
        return new InsertValuesStep(insertNode, litebridgeContext);
    }

    public InsertValuesStep insert(final String... columns) {
        final InsertNode insertNode = new InsertNode(table, null, columns);
        return new InsertValuesStep(insertNode, litebridgeContext);
    }
}
