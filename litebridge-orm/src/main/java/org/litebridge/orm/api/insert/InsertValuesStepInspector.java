package org.litebridge.orm.api.insert;

import org.litebridge.orm.api.merge.MergeTerminalInspector;
import org.litebridge.orm.engine.ast.QueryNode;

/**
 * Utility class for extracting the terminal AST query nodes of {@link InsertValuesStep} instances.
 */
public final class InsertValuesStepInspector {

    private InsertValuesStepInspector() {
    }

    /**
     * Extracts the terminal query node from the provided {@code InsertValuesStep} instance.
     *
     * @param insertValuesStep the {@code InsertValuesStep} instance from which to extract the query node
     * @return the terminal {@code QueryNode} associated with the given {@code InsertValuesStep}
     */
    public static QueryNode getNode(final InsertValuesStep insertValuesStep) {
        return MergeTerminalInspector.getNode(insertValuesStep);
    }
}
