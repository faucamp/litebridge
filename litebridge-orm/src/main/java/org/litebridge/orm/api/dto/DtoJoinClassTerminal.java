package org.litebridge.orm.api.dto;

/**
 * Terminal clause for DTO JOIN operations.
 *
 * @param <DTO> the type of the DTO
 */
public interface DtoJoinClassTerminal<DTO> {

    /**
     * Adds a JOIN clause for the specified DTO class.
     *
     * @param dtoClass the DTO class to join
     * @return the JOIN clause
     */
    DtoJoinClause<DTO> join(final Class<?> dtoClass);
}
