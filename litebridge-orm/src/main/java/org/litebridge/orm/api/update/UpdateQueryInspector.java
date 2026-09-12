package org.litebridge.orm.api.update;

import org.litebridge.orm.engine.ast.QueryNode;

/**
 * Utility for extracting the underlying {@link QueryNode} from an {@link UpdateQuery}.
 */
public final class UpdateQueryInspector {

    private UpdateQueryInspector() {
    }

    /**
     * Extracts the AST {@link QueryNode} from the given update query.
     *
     * @param updateQuery the update query to extract the node from
     * @return the corresponding {@link QueryNode}
     */
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
