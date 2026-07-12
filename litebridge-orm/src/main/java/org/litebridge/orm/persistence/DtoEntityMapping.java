package org.litebridge.orm.persistence;

import org.litebridge.orm.api.spec.FieldSpec;

import java.util.Map;

/**
 * Mapping between an entity class and its DTO fields.
 *
 * @param entityClass        the entity class
 * @param dtoEntityFieldMap the mapping between DTO fields and entity fields
 */
public record DtoEntityMapping(Class<?> entityClass, Map<FieldSpec, FieldSpec> dtoEntityFieldMap) {
}
