package org.litebridge.orm.api.select;

public interface OrderByClause<DTO,
        SELF extends OrderByClause<DTO, SELF, OBCC>,
        OBCC extends OrderByClauseChain<DTO, SELF, OBCC>> {

    /**
     * Specifies an ascending order for the current ordering clause in a query.
     *
     * @return an {@link OrderByClauseTerminal} instance with ascending order applied
     */
    OBCC asc();

    /**
     * Specifies a descending order for the current ordering clause in a query.
     *
     * @return an {@link OrderByClauseTerminal} instance with descending order applied
     */
    OBCC desc();

}
