package org.litebridgedb.orm.api.select;

import org.litebridgedb.orm.api.spec.FieldColumnSpec;

public interface HavingClauseTerminal<DTO,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>>

        extends OrderByClauseTerminal<DTO> {

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
