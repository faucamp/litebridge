package org.litebridge.orm.api.select;

/**
 * Terminal clause in a fluent API for constructing SQL JOIN statements.
 * <p>
 * This interface extends {@link WhereClauseTerminal} to allow specifying WHERE conditions
 * and transitioning to subsequent query stages such as ORDER BY clauses. It also integrates
 * with other clauses like JOIN, JOIN conditions, and WHERE conditions to build complex
 * SQL queries programmatically.
 *
 * @param <DTO>  the data transfer object (DTO) type representing the result of the query
 * @param <JC>   the type of the JOIN clause used in query construction
 * @param <JCC>  the type of the JOIN condition clause for specifying JOIN conditions
 * @param <JCCT> the terminal type of the JOIN condition clause
 * @param <WCC>  the type of the WHERE condition clause used for query filtering
 * @param <WCCT> the terminal type of the WHERE condition clause
 * @param <OBC>  the type of the ORDER BY clause defining result sorting
 * @param <OBCC> the type of the ORDER BY clause chain for chaining multiple sorting expressions
 */
public interface JoinClauseTerminal<DTO,
        JC extends JoinClause<DTO, JCC, JCCT>,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>,
        WCC extends WhereConditionClause<DTO, WCC, WCCT, OBC, OBCC>,
        WCCT extends WhereConditionClauseTerminal<DTO, WCC, WCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends WhereClauseTerminal<DTO, OBC, OBCC> {

    /**
     * Starts a WHERE clause for the SQL query.
     * <p>
     * Adds a filtering condition to the SQL query based on the specified column.
     * This method is part of a fluent API for building SQL queries and transitions
     * to the next stage where additional filtering conditions can be specified.
     *
     * @param column the name of the column to apply the filtering condition on
     * @return an instance of {@code WCC} representing the next stage of the where condition clause
     */
    WCC where(final String column);

}
