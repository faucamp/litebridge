package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;

/**
 * Interface for mapping database rows to Data Transfer Objects (DTOs).
 * Provides methods to convert a {@link Row} object into an instance of the specified DTO class.
 */
public interface DtoMapper {

    /**
     * Converts a database row into an instance of the specified Data Transfer Object (DTO) class.
     * This method maps the values in the given {@code Row} to the properties of the specified DTO class.
     * If a cache is provided via {@code dtoCache}, it may be used to retrieve or store pre-existing DTO instances.
     *
     * @param <DTO>    the type of the Data Transfer Object to be returned
     * @param row      the database row containing column-value pairs to be mapped; can be {@code null}
     * @param dtoClass the class type of the DTO to create; cannot be {@code null}
     * @param dtoCache the cache to store or retrieve DTO instances during the conversion; cannot be {@code null}
     * @return an instance of the specified DTO type populated with values from the given row,
     * or {@code null} if the row is {@code null}
     */
    <DTO> @Nullable DTO toDto(final @Nullable Row row, final Class<DTO> dtoClass, final DtoCache dtoCache);

    /**
     * Converts a database row into an instance of the specified Data Transfer Object (DTO) class.
     * This method maps the values in the given {@code Row} to the properties of the specified DTO class.
     * Uses {@link NoOpDtoCache} as the default cache mechanism.
     *
     * @param <DTO>    the type of the Data Transfer Object to be returned
     * @param row      the database row containing column-value pairs to be mapped; can be {@code null}
     * @param dtoClass the class type of the DTO to create; cannot be {@code null}
     * @return an instance of the specified DTO type populated with values from the given row,
     * or {@code null} if the row is {@code null}
     */
    default <DTO> @Nullable DTO toDto(final @Nullable Row row, final Class<DTO> dtoClass) {
        return toDto(row, dtoClass, NoOpDtoCache.INSTANCE);
    }
}
