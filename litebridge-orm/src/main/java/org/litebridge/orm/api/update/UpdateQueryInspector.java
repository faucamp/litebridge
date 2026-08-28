package org.litebridge.orm.api.update;

import org.litebridge.orm.api.select.ast.QueryNode;

public final class UpdateQueryInspector {

    private UpdateQueryInspector() {
    }

    public static QueryNode getNode(final UpdateQuery updateQuery) {
        return switch (updateQuery) {
            case DtoUpdateWhereConditionClauseTerminalImpl<?> dtoUpdateWhereConditionClauseTerminalImpl ->
                    dtoUpdateWhereConditionClauseTerminalImpl.node();
            case SqlUpdateWhereConditionClauseTerminalImpl sqlUpdateWhereConditionClauseTerminalImpl ->
                    sqlUpdateWhereConditionClauseTerminalImpl.node();
            case DtoUpdateStep<?> dtoUpdateStep -> dtoUpdateStep.node();
            case SqlUpdateStep sqlUpdateStep -> sqlUpdateStep.node();
        };
    }
}
