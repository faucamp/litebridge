package org.litebridge.orm.api.select;

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
 * @param <GBCT> the terminal type of the GROUP BY clause, marking the end of GROUP BY clause
 * @param <HCC>  the type of the HAVING condition clause for further filtering
 * @param <HCCT> the terminal type of the HAVING condition clause, marking the end of HAVING conditions
 * @param <WCC>  the type of the WHERE condition clause that facilitates further condition chaining
 * @param <SELF> the type of the implementing subclass for enabling fluent API style method chaining
 * @param <OBC>  the type of the ORDER BY clause for specifying result ordering
 * @param <OBCC> the type of the ORDER BY clause chain for chaining multiple ordering instructions
 */
public interface WhereConditionClauseTerminal<DTO,
        WCC extends WhereConditionClause<DTO, WCC, SELF, GBCT, HCC, HCCT, OBC, OBCC>,
        SELF extends WhereConditionClauseTerminal<DTO, WCC, SELF, GBCT, HCC, HCCT, OBC, OBCC>,
        GBCT extends GroupByClauseTerminal<DTO, HCC, HCCT, OBC, OBCC>,
        HCC extends HavingConditionClause<DTO, HCC, HCCT, OBC, OBCC>,
        HCCT extends HavingConditionClauseTerminal<DTO, HCC, HCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends ConditionClauseTerminal<DTO, WCC, SELF>,
        WhereClauseTerminal<DTO, GBCT, HCC, HCCT, OBC, OBCC> {
}
