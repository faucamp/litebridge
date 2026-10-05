package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;

/**
 * Cache key identifying a compiled mapping plan by target DTO class, context DTO class, and row structural hash.
 *
 * @param dtoClass         the target DTO class
 * @param contextDtoClass  the context DTO class, or null
 * @param rowStructureHash the structural hash of the result row
 */
public record MappingPlanKey(Class<?> dtoClass, @Nullable Class<?> contextDtoClass, int rowStructureHash) {
}
