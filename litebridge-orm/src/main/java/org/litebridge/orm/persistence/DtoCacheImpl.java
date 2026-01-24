package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.type.WeakIdentityMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DtoCacheImpl implements DtoCache {

    private final Map<Class<?>, Map<Integer, Object>> cache = new WeakIdentityMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <DTO> @Nullable DTO get(final Class<DTO> dtoClass, final Object[] id) {
        return (DTO) cache.computeIfAbsent(dtoClass, cls -> new HashMap<>())
                .get(Arrays.hashCode(id));
    }

    @Override
    public void put(final Object[] id, final @Nullable Object dto) {
        if (dto == null) {
            return;
        }

        cache.computeIfAbsent(dto.getClass(), cls -> new HashMap<>())
                .put(Arrays.hashCode(id), dto);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <DTO> List<DTO> getAll(final Class<DTO> dtoClass) {
        final Map<Integer, Object> idHashDtoMap = cache.get(dtoClass);

        if (idHashDtoMap == null) {
            return null;
        }

        return (List<DTO>) idHashDtoMap.values().stream().toList();
    }
}
