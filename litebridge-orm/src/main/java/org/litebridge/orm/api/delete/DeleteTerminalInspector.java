package org.litebridge.orm.api.delete;

import org.litebridge.orm.engine.ast.QueryNode;

public final class DeleteTerminalInspector {

    private DeleteTerminalInspector() {
    }

    public static QueryNode getNode(final DeleteTerminal deleteTerminal) {
        return switch (deleteTerminal) {
            case DtoDeleteStart<?> dtoDeleteStart -> dtoDeleteStart.node();
            case DtoDeleteWhereConditionClauseTerminalImpl<?> dtoUpdateWhereConditionClauseTerminalImpl ->
                    dtoUpdateWhereConditionClauseTerminalImpl.node();
            case SqlDeleteStart sqlDeleteStart -> sqlDeleteStart.node();
            case SqlDeleteWhereConditionClauseTerminalImpl sqlUpdateWhereConditionClauseTerminalImpl ->
                    sqlUpdateWhereConditionClauseTerminalImpl.node();
        };
    }
}
