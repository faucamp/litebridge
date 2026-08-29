package org.litebridge.orm.api.insert;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.engine.ast.InsertValuesNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;

public final class InsertValuesStep {

    private final QueryNode node;
    private final LitebridgeContext litebridgeContext;

    public InsertValuesStep(final QueryNode node,
                            final LitebridgeContext litebridgeContext) {
        this.node = node;
        this.litebridgeContext = litebridgeContext;
    }

    public InsertValuesStep values(final Object @Nullable ... values) {
        return new InsertValuesStep(new InsertValuesNode(node, values), litebridgeContext);
    }

    QueryNode node() {
        return node;
    }
}
