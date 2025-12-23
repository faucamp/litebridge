package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;

import java.util.Map;

public interface DtoMapper<DTO> {

    @Nullable
    DTO toDto(final @Nullable Map<String, Object> row);

}
