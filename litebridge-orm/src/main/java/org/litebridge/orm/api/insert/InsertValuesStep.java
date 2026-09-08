package org.litebridge.orm.api.insert;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.merge.MergeTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.InsertValuesNode;
import org.litebridge.orm.engine.ast.QueryNode;

public final class InsertValuesStep extends MergeTerminal {

    private final LitebridgeContext litebridgeContext;

    public InsertValuesStep(final QueryNode node,
                            final LitebridgeContext litebridgeContext) {
        super(node);
        this.litebridgeContext = litebridgeContext;
    }

    public InsertValuesStep values(final Object @Nullable ... values) {
        return new InsertValuesStep(new InsertValuesNode(node, values), litebridgeContext);
    }
}
