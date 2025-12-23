package org.litebridge.orm.api.select;

public interface DtoMappingSelectTerminal<DTO> extends SelectTerminal<DTO> {

    <T> T toDto(DTO result, Class<T> dtoClass);

}
