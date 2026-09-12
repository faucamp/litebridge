package org.litebridge.orm.api.delete;

import org.litebridge.orm.engine.ast.QueryNode;

/**
 * Utility inspector for extracting the AST {@link QueryNode} from a {@link DeleteTerminal}.
 */
public final class DeleteTerminalInspector {

    private DeleteTerminalInspector() {
    }

    /**
     * Extracts the AST {@link QueryNode} from the specified {@link DeleteTerminal}.
     *
     * @param deleteTerminal the delete terminal step
     * @return the underlying query node
     */
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
