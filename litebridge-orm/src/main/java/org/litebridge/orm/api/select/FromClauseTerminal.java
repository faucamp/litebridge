package org.litebridge.orm.api.select;

/**
 * Terminal stage of the FROM clause in a fluent API for constructing SQL queries.
 * <p>
 * This interface allows transitioning from the FROM clause to other query stages, such as JOIN,
 * WHERE, or ORDER BY clauses, while building a type-safe and structured SQL query programmatically.
 * <p>
 * It extends {@link JoinClauseTerminal} to provide seamless integration with JOIN operations and
 * subsequent query clauses.
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
public interface FromClauseTerminal<DTO,
        JC extends JoinClause<DTO, JCC, JCCT>,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>,
        WCC extends WhereConditionClause<DTO, WCC, WCCT, OBC, OBCC>,
        WCCT extends WhereConditionClauseTerminal<DTO, WCC, WCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends JoinClauseTerminal<DTO, JC, JCC, JCCT, WCC, WCCT, OBC, OBCC> {

}
