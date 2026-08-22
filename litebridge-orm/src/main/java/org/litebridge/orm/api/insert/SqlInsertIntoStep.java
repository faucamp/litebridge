package org.litebridge.orm.api.insert;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.select.ast.InsertNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

public final class SqlInsertIntoStep extends InsertIntoStep {

    private final @Nullable String tableName;

    public SqlInsertIntoStep(final String tableName,
                             final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
        this.tableName = tableName;
    }

    public InsertValuesStep into(final String... columns) {
        final InsertNode insertNode = new InsertNode(tableName, null, columns);
        return new InsertValuesStep(insertNode, litebridgeContext);
    }

    @Override
    public InsertValuesStep into(final ExpressionSpec... expressions) {
        final InsertNode insertNode = new InsertNode(tableName, null, expressions);
        return new InsertValuesStep(insertNode, litebridgeContext);
    }
}
