package org.litebridgedb.orm.api.select;

/**
 * Represents a join clause in a SQL query, allowing the definition of join operations
 * between tables.
 * <p>
 * This interface is part of a fluent API and provides methods to specify the join conditions.
 *
 * @param <DTO>  the data transfer object (DTO) type that represents the result of the query
 * @param <JCC>  the type of the join condition clause used to define conditions for the join
 * @param <JCCT> the terminal type of the join condition clause, marking the end of the join conditions
 */
public interface JoinClause<DTO,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>> {

}
