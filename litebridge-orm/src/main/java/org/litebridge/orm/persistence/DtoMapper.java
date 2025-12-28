package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;

public interface DtoMapper {

    <DTO> @Nullable DTO toDto(final @Nullable Row row, final Class<DTO> dtoClass, final DtoCache dtoCache);

    default <DTO> @Nullable DTO toDto(final @Nullable Row row, final Class<DTO> dtoClass) {
        return toDto(row, dtoClass, NoOpDtoCache.INSTANCE);
    }
}
