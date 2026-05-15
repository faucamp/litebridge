package org.litebridgedb.orm.persistence;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A no-operation implementation of the {@link DtoCache} interface.
 * This implementation does not perform any caching and is designed
 * to always return {@code null} or perform no action when its methods are invoked.
 * It can be used in scenarios where caching is not required or desirable.
 * <p>
 * This class is implemented as a singleton, and the single instance can be accessed
 * via the {@link #INSTANCE} field.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class NoOpDtoCache implements DtoCache {

    /**
     * The singleton instance of the {@link NoOpDtoCache} class.
     * This instance represents a no-operation implementation of the {@link DtoCache} interface
     * that does not perform any caching functionality. All methods invoked on this instance
     * either return {@code null} or take no action.
     * <p>
     * Use this constant when caching is not required or should be explicitly disabled,
     * ensuring no resources are consumed or maintained for caching operations.
     * <p>
     * This instance is immutable, stateless, and thread-safe.
     */
    public static final NoOpDtoCache INSTANCE = new NoOpDtoCache();

    private NoOpDtoCache() {
    }

    @Override
    public <DTO> @Nullable DTO get(final Class<DTO> dtoClass, final List<Object> id) {
        return null;
    }

    @Override
    public void put(final List<Object> id, @Nullable final Object dto) {
    }

    @Override
    public @Nullable <DTO> List<DTO> getAll(final Class<DTO> dtoClass) {
        return null;
    }
}
