package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;

import java.util.Collection;
import java.util.Objects;

final class ConditionNodeUtil {

    private ConditionNodeUtil() {
    }

    static Object valueStructuralKey(final @Nullable Object value) {
        return switch (value) {
            case Collection<?> collection -> collection.size();
            case QueryNode queryNode -> queryNode;
            case SelectTerminal<?> st -> Objects.requireNonNull(SelectTerminalInspector.getNode(st));
            case null, default -> 1;
        };
    }
}
