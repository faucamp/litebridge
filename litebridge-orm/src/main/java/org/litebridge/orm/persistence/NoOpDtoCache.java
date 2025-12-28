package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;

public final class NoOpDtoCache implements DtoCache {

    public static final NoOpDtoCache INSTANCE = new NoOpDtoCache();

    private NoOpDtoCache() {
    }

    @Override
    public <DTO> @Nullable DTO get(final Class<DTO> dtoClass, final Object[] id) {
        return null;
    }

    @Override
    public void put(final Object[] id, @Nullable final Object dto) {
    }
}
