package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;

public interface DtoMapper<DTO> {

    @Nullable
    DTO toDto(final @Nullable LinkedHashMap<String, Object> row);

}
