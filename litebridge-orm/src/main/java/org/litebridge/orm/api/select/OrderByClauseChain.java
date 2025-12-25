package org.litebridge.orm.api.select;

public interface OrderByClauseChain<DTO,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends OrderByClauseTerminal<DTO> {

    /**
     * Adds another ordering expression which again requires an explicit direction.
     * Each call to this method appends another ordering expression.
     *
     * @param columns Table column(s) or DTO field(s) to order by
     * @return a selector chain with ordering applied
     */
    OBC then(final String... columns);
}
