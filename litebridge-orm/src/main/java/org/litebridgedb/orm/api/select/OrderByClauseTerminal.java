package org.litebridgedb.orm.api.select;

/**
 * Terminal clause of an ORDER BY operation.
 * <p>
 * This interface enables the application of a LIMIT clause to restrict the number of rows in the query result.
 *
 * @param <DTO> the data transfer object (DTO) type that represents the result of the query
 */
public interface OrderByClauseTerminal<DTO> extends LimitClauseTerminal<DTO> {

    /**
     * Limits the number of rows returned in a query result.
     *
     * @param limit the maximum number of rows to be returned. Must be a positive integer.
     * @return an instance of {@code LimitClauseTerminal} to allow for further query customization or execution.
     */
    LimitClauseTerminal<DTO> limit(final int limit);
}
