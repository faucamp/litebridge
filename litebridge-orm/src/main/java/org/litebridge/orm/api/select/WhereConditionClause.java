package org.litebridge.orm.api.select;

/**
 * Represents a WHERE condition clause in a query, allowing the composition of conditional
 * expressions for filtering data in a fluent and type-safe manner.
 * <p>
 * This interface extends {@link ConditionClause} and enables chaining with terminal operations, as well as integration
 * with ORDER BY clauses through the associated types.
 *
 * @param <DTO>  the type of the data transfer object (DTO) associated with the query
 * @param <SELF> the type of the implementing subclass to allow type-safe chaining of WHERE clauses
 * @param <WCCT> the type of the terminal WHERE condition clause
 * @param <OBC>  the type of the ORDER BY clause associated with this WHERE clause
 * @param <OBCC> the type of the ORDER BY clause chain for further chaining of ordering expressions
 */
public interface WhereConditionClause<DTO,
        SELF extends WhereConditionClause<DTO, SELF, WCCT, OBC, OBCC>,
        WCCT extends WhereConditionClauseTerminal<DTO, SELF, WCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends ConditionClause<DTO, SELF, WCCT> {

}
