package org.litebridge.orm.api.select;

public interface OrderByClauseChain<DTO> extends OrderByClauseTerminal<DTO> {

    /**
     * Adds another ordering expression which again requires an explicit direction.
     * Each call to this method appends another ordering expression.
     *
     * @param columns Table column(s) or DTO field(s) to order by
     * @return a selector chain with ordering applied
     */
    OrderByClause<DTO> then(final String... columns);
}
