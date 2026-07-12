package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The DtoCache interface defines methods for caching Data Transfer Objects (DTOs). It provides functionality
 * for retrieving and storing DTOs using their class type and an identifier. Implementations of this interface
 * can manage caching strategies for efficient reuse of DTO instances.
 * <p>
 * This interface has the following key methods:
 * <ul>
 *   <li>{@link #get(Class, List)}: Retrieves a cached DTO of the specified class type and identifier, or
 * {@code null} if no matching DTO is found in the cache.</li>
 *   <li>{@link #put(List, Object)}: Stores a new DTO in the cache associated with the provided identifier.</li>
 * </ul>
 * <p>
 * The interface is designed as a sealed interface with two implementations:
 * <ul>
 *   <li>{@code NoOpDtoCache}: A no-operation implementation that does not perform any actual caching.</li>
 *   <li>{@code DtoCacheImpl}: A concrete implementation that uses a {@code Map}-based caching mechanism.</li>
 * </ul>
 */
public sealed interface DtoCache permits NoOpDtoCache, DtoCacheImpl {

    /**
     * Retrieves a cached Data Transfer Object (DTO) of the specified class type and identifier.
     * If no matching DTO is found in the cache, this method returns {@code null}.
     *
     * @param <DTO>    The type of the Data Transfer Object.
     * @param dtoClass The class type of the DTO to retrieve.
     * @param id       The identifier used to locate the DTO in the cache. This is typically
     *                 a list of keys representing a unique identifier for the DTO.
     * @return The cached instance of the specified DTO type matching the given identifier,
     * or {@code null} if no such DTO is found.
     */
    <DTO> @Nullable DTO get(Class<DTO> dtoClass, List<Object> id);

    /**
     * Stores a Data Transfer Object (DTO) in the cache associated with the provided identifier.
     * The identifier is typically a list of keys that uniquely represents the DTO. If the
     * provided DTO is {@code null}, this method may effectively remove any existing entry
     * associated with the given identifier, depending on the implementation.
     *
     * @param id  The identifier used to associate the DTO with a cached entry. This is
     *            typically a list of keys representing a unique identifier for the DTO.
     * @param dto The Data Transfer Object to store in the cache. It can be {@code null}
     *            if the entry should be removed or not stored.
     */
    void put(List<Object> id, @Nullable Object dto);

    /**
     * Retrieves a list of all cached Data Transfer Objects (DTOs) of the specified class type.
     * If no DTOs of the given type are found, this method returns {@code null}.
     *
     * @param <DTO>    The type of the Data Transfer Objects.
     * @param dtoClass The class type of the DTOs to retrieve.
     * @return A list of DTOs of the specified type currently in the cache, or {@code null}
     * if no DTOs of the given class type are found.
     */
    <DTO> @Nullable List<DTO> getAll(Class<DTO> dtoClass);
}
