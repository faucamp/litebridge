package org.litebridgedb.orm.api.select;

/**
 * Starting point for constructing a SQL query by specifying the source table and schema.
 * <p>
 * This interface is part of a flexible and type-safe fluent API that allows users
 * to construct complex SQL queries in stages.
 *
 * @param <DTO>  the data transfer object (DTO) type that represents the result of the query
 * @param <FCT>  the type of the terminal from clause to transition to the next clause or stage
 * @param <JC>   the type of join clause used for specifying table joins
 * @param <JCC>  the type of join condition clause used for providing conditions in joins
 * @param <JCCT> the terminal type of join condition clause, marking the end of join conditions
 * @param <WCC>  the type of where condition clause used for applying filters
 * @param <WCCT> the terminal type of where condition clause, marking the end of filter conditions
 * @param <OBC>  the type of order by clause used to define the ordering of results
 * @param <OBCC> the type of order by clause chain for chaining multiple orderings
 */
public interface FromClause<DTO,
        FCT extends FromClauseTerminal<DTO, JC, JCC, JCCT, WCC, WCCT, OBC, OBCC>,
        JC extends JoinClause<DTO, JCC, JCCT>,
        JCC extends JoinConditionClause<DTO, JCC, JCCT>,
        JCCT extends JoinConditionClauseTerminal<DTO, JCC, JCCT>,
        WCC extends WhereConditionClause<DTO, WCC, WCCT, OBC, OBCC>,
        WCCT extends WhereConditionClauseTerminal<DTO, WCC, WCCT, OBC, OBCC>,
        OBC extends OrderByClause<DTO, OBC, OBCC>,
        OBCC extends OrderByClauseChain<DTO, OBC, OBCC>> {


    /**
     * Specifies the source table and schema for the SQL query, setting the base
     * table and expressions used in the query construction.
     * <p>
     * If the table is not already registered, it is created and associated with an empty schema and given
     * table name.
     *
     * @param table the name of the table within the specified schema
     * @return an instance of {@code SqlFromClauseTerminal} to allow further query configuration
     */
    FCT from(final String table);
}
