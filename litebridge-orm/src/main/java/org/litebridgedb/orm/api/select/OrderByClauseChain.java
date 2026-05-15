package org.litebridgedb.orm.api.select;

/**
 * Chainable clause for constructing complex ORDER BY clauses in a type-safe
 * and fluent API for SQL query construction.
 * <p>
 * This interface allows chaining of multiple ORDER BY expressions, enabling the specification
 * of ordering for various columns or fields with explicit directions.
 *
 * @param <DTO>  the data transfer object (DTO) type that represents the result of the query
 * @param <OBC>  the type of the {@code OrderByClause} used to define the ordering for a single set of columns
 * @param <OBCC> the type of the {@code OrderByClauseChain} used to continue chaining additional ordering clauses
 */
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
