package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;

public sealed interface DtoCache permits NoOpDtoCache, DtoCacheImpl {

    <DTO> @Nullable DTO get(Class<DTO> dtoClass, Object[] id);

    void put(Object[] id, @Nullable Object dto);
}
