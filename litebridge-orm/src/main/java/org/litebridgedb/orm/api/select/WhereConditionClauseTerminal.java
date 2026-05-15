package org.litebridgedb.orm.api.select;

/**
 * Terminal interface for a WHERE condition clause in SQL query construction.
 * <p>
 * This interface facilitates the creation of type-safe and fluent query-building methods,
 * supporting advanced chaining mechanisms for WHERE conditions and ORDER BY clauses.
 * It combines the capabilities of {@link ConditionClauseTerminal} to mark the end of
 * logical condition construction and {@link WhereClauseTerminal} to transition into
 * ORDER BY clause construction.
 * <p>
 * This interface is parameterized to provide flexibility and maintain strict type safety,
 * ensuring the seamless chaining of compatible query-building stages.
 *
 * @param <DTO>  the type of the Data Transfer Object (DTO) representing the query result
 * @param <WCC>  the type of the WHERE condition clause that facilitates further condition chaining
 * @param <SELF> the type of the implementing subclass for enabling fluent API style method chaining
 * @param <OBC>  the type of the ORDER BY clause for specifying result ordering
 * @param <OBCC> the type of the ORDER BY clause chain for chaining multiple ordering instructions
 */
public interface WhereConditionClauseTerminal<DTO,
        WCC extends WhereConditionClause<DTO, WCC, SELF, OBC, OBCC>,
        SELF extends WhereConditionClauseTerminal<DTO, WCC, SELF, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends ConditionClauseTerminal<DTO, WCC, SELF>,
        WhereClauseTerminal<DTO, OBC, OBCC> {

}
