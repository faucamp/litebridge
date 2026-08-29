package org.litebridge.orm.api.insert;

import org.litebridge.orm.engine.ast.QueryNode;

public final class InsertValuesStepInspector {

    private InsertValuesStepInspector() {
    }

    public static QueryNode getNode(final InsertValuesStep insertValuesStep) {
        return insertValuesStep.node();
    }
}
