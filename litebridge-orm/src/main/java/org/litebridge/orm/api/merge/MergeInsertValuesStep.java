package org.litebridge.orm.api.merge;

import org.litebridge.orm.api.select.ast.InsertNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

public class MergeInsertValuesStep {

    private final InsertNode insertNode;
    private final LitebridgeContext litebridgeContext;

    public MergeInsertValuesStep(final InsertNode insertNode, final LitebridgeContext litebridgeContext) {
        this.insertNode = insertNode;
        this.litebridgeContext = litebridgeContext;
    }

    public MergeTerminal values(final ExpressionSpec expression, final ExpressionSpec... otherExpressions) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public MergeTerminal values(final Object... values) {
        return new MergeTerminal(insertNode);
    }
}
