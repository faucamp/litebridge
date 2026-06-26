package org.litebridgedb.orm.api.select;

import org.litebridgedb.orm.api.spec.FieldColumnSpec;

/**
 * Terminal clause for constructing SQL WHERE conditions, allowing transitions
 * to GROUP BY or ORDER BY stages in a fluent and type-safe query-building process.
 *
 * @param <DTO>  the data transfer object (DTO) type that represents the result of the query
 * @param <GBCT> the terminal type of the GROUP BY clause, marking the end of GROUP BY clause
 * @param <HCC>  the type of the HAVING condition clause for further filtering
 * @param <HCCT> the terminal type of the HAVING condition clause, marking the end of HAVING conditions
 * @param <OBC>  the type of the order by clause used to define the ordering of query results
 * @param <OBCC> the type of the order by clause chain for chaining multiple ordering expressions
 */
public interface WhereClauseTerminal<DTO,
        GBCT extends GroupByClauseTerminal<DTO, HCC, HCCT, OBC, OBCC>,
        HCC extends HavingConditionClause<DTO, HCC, HCCT, OBC, OBCC>,
        HCCT extends HavingConditionClauseTerminal<DTO, HCC, HCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends OrderByClauseTerminal<DTO> {

    /**
     * Adds a GROUP BY clause to the query, specifying the lhs(s) to group the results by.
     *
     * @param columns the lhs(s) to group the results by.
     *                Each lhs must be valid for the associated table or view in the query.
     * @return an instance of the type representing the GROUP BY clause, allowing
     * further specification of grouping or transitioning to the next query stage.
     */
    GBCT groupBy(String... columns);

    /**
     * Adds an ORDER BY clause to the query, specifying the expressions to sort the results by.
     *
     * @param columns the expressions that determine the order of the results. Each lhs
     *                must be valid for the associated table or view in the query.
     *                The order in which the expressions are specified determines the
     *                priority of ordering.
     * @return an instance of the type representing the ORDER BY clause, allowing
     * further specification of ordering or transitioning to the next query stage.
     */
    OBC orderBy(String... columns);

    /**
     * Adds an ORDER BY clause to the query, specifying the fields and expressions to sort the results by.
     *
     * @param columns the field and lhs specifications that determine the order of the results.
     *                Each specification includes a mapping of a field to a lhs. The order in
     *                which the specifications are provided defines the priority of ordering.
     * @return an instance of the type representing the ORDER BY clause, allowing further
     * specification of ordering or transitioning to the next query stage.
     */
    OBC orderBy(FieldColumnSpec... columns);
}
