package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;

public interface DtoMapper {

    @Nullable
    <DTO> DTO toDto(final @Nullable Row row, final Class<DTO> dtoClass);

}
