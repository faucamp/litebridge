package org.litebridge.orm.api.select;

/**
 * Terminal state of a join condition clause.
 * <p>
 * This interface is designed to mark the end of a join condition clause and
 * allows transitioning to subsequent stages of query construction.
 * <p>
 * This interface extends {@code ConditionClauseTerminal}, inheriting methods that support chaining
 * and finalization of condition clauses, as well as further progression in the query construction process.
 * It works in conjunction with other interfaces such as {@link JoinConditionClause} and {@link JoinClause}
 * to enable the smooth and structured creation of SQL queries involving join operations.
 * <p>
 * This type is used to finalize join conditions and transition to the next relevant stage, such as
 * adding additional filtering conditions or defining result ordering.
 *
 * @param <DTO>  the data transfer object type representing the result of the query
 * @param <JCC>  the type of join condition clause leading to this terminal clause
 * @param <SELF> the type of the implementing subclass for fluent query construction
 */
public interface JoinConditionClauseTerminal<DTO,
        JCC extends JoinConditionClause<DTO, JCC, SELF>,
        SELF extends JoinConditionClauseTerminal<DTO, JCC, SELF>>

        extends ConditionClauseTerminal<DTO, JCC, SELF> {

}
