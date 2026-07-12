package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class DtoCacheImpl implements DtoCache {

    private final Map<Class<?>, Map<List<Object>, Object>> cache = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <DTO> @Nullable DTO get(final Class<DTO> dtoClass, final List<Object> id) {
        return (DTO) cache.computeIfAbsent(dtoClass, cls -> new HashMap<>())
                .get(id);
    }

    @Override
    public void put(final List<Object> id, final @Nullable Object dto) {
        if (dto == null) {
            return;
        }

        cache.computeIfAbsent(dto.getClass(), cls -> new HashMap<>())
                .put(id, dto);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <DTO> List<DTO> getAll(final Class<DTO> dtoClass) {
        final Map<List<Object>, Object> idDtoMap = cache.get(dtoClass);

        if (idDtoMap == null) {
            return null;
        }

        return (List<DTO>) idDtoMap.values().stream().toList();
    }
}
