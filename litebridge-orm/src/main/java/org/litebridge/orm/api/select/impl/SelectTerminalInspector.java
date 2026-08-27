package org.litebridge.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.ast.QueryNode;

/**
 * Utility class for inspecting {@link SelectTerminal} instances.
 */
public final class SelectTerminalInspector {

    private SelectTerminalInspector() {
    }

    /**
     * Retrieves the {@link QueryNode} associated with the given {@link SelectTerminal}.
     *
     * @param selectTerminal the {@link SelectTerminal} to inspect
     * @return the {@link QueryNode} associated with the given {@link SelectTerminal}
     */
    public static @Nullable QueryNode getNode(final SelectTerminal<?> selectTerminal) {
        if (selectTerminal instanceof DelegatingSelectTerminal<?> delegatingSelector) {
            return delegatingSelector.node();
        } else {
            throw new IllegalStateException("Unsupported select terminal type: " + selectTerminal.getClass().getName());
        }
    }
}
