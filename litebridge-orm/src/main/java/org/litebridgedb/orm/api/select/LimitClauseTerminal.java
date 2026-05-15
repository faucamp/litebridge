package org.litebridgedb.orm.api.select;

/**
 * Terminal clause for applying a LIMIT clause in a SQL query.
 * <p>
 * This interface provides functionality to set an offset for skipping a
 * specified number of rows in the result set, which is commonly used for
 * implementing pagination.
 *
 * @param <DTO> the data transfer object (DTO) type that represents the
 *              result of the query
 */
public interface LimitClauseTerminal<DTO> extends SelectTerminal<DTO> {

    /**
     * Sets an offset to skip a specified number of rows in the result set.
     * This is typically used in combination with a limit clause for implementing
     * pagination in queries.
     *
     * @param offset the number of rows to skip in the result set, must be a non-negative integer
     * @return an instance of {@code SelectTerminal<DTO>} to continue building or executing the query
     */
    SelectTerminal<DTO> offset(int offset);

}
