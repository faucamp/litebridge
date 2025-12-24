package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class DtoMapperRegistry {

    private final Map<Class<?>, DtoMapper<?>> dtoMappers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <DTO> @Nullable DtoMapper<DTO> getDtoMapper(final Class<DTO> dtoClass) {
        return (DtoMapper<DTO>) dtoMappers.get(dtoClass);
    }

    @SuppressWarnings("unchecked")
    public <DTO> DtoMapper<DTO> ensureDtoMapper(final Class<DTO> dtoClass, final Supplier<DtoMapper<DTO>> supplier) {
        return (DtoMapper<DTO>) dtoMappers.computeIfAbsent(dtoClass, key -> supplier.get());
    }

    public <DTO> DtoMapper<DTO> getDtoMapperOrThrow(final Class<DTO> dtoClass) {
        return ObjectUtils.requireNonNull(getDtoMapper(dtoClass), "No DTO mapper found for class: " + dtoClass.getName());
    }

    <DTO> void addDtoMapper(final Class<DTO> dtoClass, final DtoMapper<DTO> dtoMapper) {
        dtoMappers.put(dtoClass, dtoMapper);
    }
}
