package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;

import java.util.Collection;
import java.util.Objects;

/**
 * Utility methods for working with condition nodes.
 */
final class ConditionNodeUtil {

    private ConditionNodeUtil() {
    }

    /**
     * Returns a structural key for the given value (the "shape" of the value).
     *
     * @param value the value to get the structural key for
     * @return the structural key, used duing AST hash value calculation
     */
    static Object valueStructuralKey(final @Nullable Object value) {
        return switch (value) {
            case Collection<?> collection -> collection.size();
            case QueryNode queryNode -> queryNode;
            case SelectTerminal<?> st -> Objects.requireNonNull(SelectTerminalInspector.getNode(st));
            case null, default -> 1;
        };
    }
}
