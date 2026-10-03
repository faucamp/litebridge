package org.litebridge.db.oracle.api.insert;

import org.litebridge.orm.engine.ast.InsertValuesNode;

import java.util.List;

public final class InsertAllStepInspector {

    private InsertAllStepInspector() {
    }

    public static List<InsertValuesNode> insertValuesNodes(final InsertAllStep insertAllStep) {
        return insertAllStep.insertValuesNodes();
    }
}
