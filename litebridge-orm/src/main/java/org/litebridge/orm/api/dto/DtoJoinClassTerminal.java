package org.litebridge.orm.api.dto;

public interface DtoJoinClassTerminal<DTO> {

    DtoJoinClause<DTO> join(final Class<?> dtoClass);
}
