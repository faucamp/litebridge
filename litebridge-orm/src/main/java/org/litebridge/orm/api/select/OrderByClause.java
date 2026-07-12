package org.litebridge.orm.api.select;

/**
 * Clause in a SQL query for specifying ordering of rows based on one or more expressions
 * or fields.
 * <p>
 * This interface provides methods to define the order by direction, either ascending or
 * descending, in a type-safe and fluent manner.
 *
 * @param <DTO>  the data transfer object (DTO) type that represents the result of the query
 * @param <SELF> the type of the current {@code OrderByClause} implementation
 * @param <OBCC> the type of the {@code OrderByClauseChain} for chaining additional order by clauses
 */
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
