package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;

/**
 * The DtoCache interface defines methods for caching Data Transfer Objects (DTOs). It provides functionality
 * for retrieving and storing DTOs using their class type and an identifier. Implementations of this interface
 * can manage caching strategies for efficient reuse of DTO instances.
 * <p>
 * This interface has the following key methods:
 * - {@link #get(Class, Object[])}: Retrieves a cached DTO of the specified class type and identifier, or
 * {@code null} if no matching DTO is found in the cache.
 * - {@link #put(Object[], Object)}: Stores a new DTO in the cache associated with the provided identifier.
 * <p>
 * The interface is designed as a sealed interface with two implementations:
 * - {@code NoOpDtoCache}: A no-operation implementation that does not perform any actual caching.
 * - {@code DtoCacheImpl}: A concrete implementation that uses a {@code Map}-based caching mechanism.
 */
public sealed interface DtoCache permits NoOpDtoCache, DtoCacheImpl {

    <DTO> @Nullable DTO get(Class<DTO> dtoClass, Object[] id);

    void put(Object[] id, @Nullable Object dto);
}
