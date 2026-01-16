package org.litebridge.orm.api.select;

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

    /**
     * Adds a join ON condition to the current join clause based on the specified column.
     * The join condition constrains the relationship between the tables being joined.
     *
     * @param column the name of the column to be used in the join condition
     * @return an instance of the join condition clause to allow further configuration
     */
    JCC on(String column);

    /**
     * Adds a join USING condition to the current join clause using the specified column.
     * This method simplifies the join condition by specifying a single column that is
     * shared between two tables in the join.
     *
     * @param column the name of the column to be used for the join condition
     * @return an instance of the terminal join condition clause to finalize the join conditions
     */
    JCCT using(String column);
}
