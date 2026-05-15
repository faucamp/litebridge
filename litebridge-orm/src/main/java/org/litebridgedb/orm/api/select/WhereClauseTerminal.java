package org.litebridgedb.orm.api.select;

import org.litebridgedb.orm.api.spec.FieldColumnSpec;

/**
 * Terminal clause for constructing SQL WHERE conditions, allowing transitions
 * to ORDER BY stages in a fluent and type-safe query-building process.
 *
 * @param <DTO>  the data transfer object (DTO) type that represents the result of the query
 * @param <OBC>  the type of the order by clause used to define the ordering of query results
 * @param <OBCC> the type of the order by clause chain for chaining multiple ordering expressions
 */
public interface WhereClauseTerminal<DTO,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends OrderByClauseTerminal<DTO> {

    /**
     * Adds an ORDER BY clause to the query, specifying the columns to sort the results by.
     *
     * @param columns the columns that determine the order of the results. Each column
     *                must be valid for the associated table or view in the query.
     *                The order in which the columns are specified determines the
     *                priority of ordering.
     * @return an instance of the type representing the ORDER BY clause, allowing
     * further specification of ordering or transitioning to the next query stage.
     */
    OBC orderBy(String... columns);

    /**
     * Adds an ORDER BY clause to the query, specifying the fields and columns to sort the results by.
     *
     * @param columns the field and column specifications that determine the order of the results.
     *                Each specification includes a mapping of a field to a column. The order in
     *                which the specifications are provided defines the priority of ordering.
     * @return an instance of the type representing the ORDER BY clause, allowing further
     * specification of ordering or transitioning to the next query stage.
     */
    OBC orderBy(FieldColumnSpec... columns);
}
