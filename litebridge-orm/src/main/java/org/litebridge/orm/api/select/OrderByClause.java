package org.litebridge.orm.api.select;

public interface OrderByClause<DTO>  {

    /**
     * Specifies an ascending order for the current ordering clause in a query.
     *
     * @return an {@link OrderByClauseTerminal} instance with ascending order applied
     */
    OrderByClauseTerminal<DTO> asc();

    /**
     * Specifies a descending order for the current ordering clause in a query.
     *
     * @return an {@link OrderByClauseTerminal} instance with descending order applied
     */
    OrderByClauseTerminal<DTO> desc();

}
