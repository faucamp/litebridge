package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.QueryNode;

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
    public static QueryNode getNode(final SelectTerminal<?> selectTerminal) {
        if (selectTerminal instanceof DelegatingSelectTerminal<?> delegatingSelector) {
            return delegatingSelector.node();
        } else {
            throw new IllegalStateException("Unsupported select terminal type: " + selectTerminal.getClass().getName());
        }
    }

    /**
     * Retrieves the {@link LitebridgeContext} associated with the given {@link SelectTerminal}.
     *
     * @param selectTerminal the {@link SelectTerminal} to inspect
     * @return the {@link LitebridgeContext} associated with the given {@link SelectTerminal}
     */
    public static LitebridgeContext getLitebridgeContext(final SelectTerminal<?> selectTerminal) {
        if (selectTerminal instanceof DelegatingSelectTerminal<?> delegatingSelector) {
            return delegatingSelector.litebridgeContext();
        } else {
            throw new IllegalStateException("Unsupported select terminal type: " + selectTerminal.getClass().getName());
        }
    }

    public static SelectEngineTerminal getSelectEngineTerminal(final SelectTerminal<?> selectTerminal) {
        if (selectTerminal instanceof DelegatingSelectTerminal<?> delegatingSelector) {
            return delegatingSelector.selectEngineTerminal();
        } else {
            throw new IllegalStateException("Unsupported select terminal type: " + selectTerminal.getClass().getName());
        }
    }
}
