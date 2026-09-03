package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.tracking.FieldAccessor;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;

/**
 * A "pre-compiled" mapping plan for a specific DTO class and query specification.
 * It pre-resolves column indices, constructors, and field mappers to minimize overhead during row processing.
 *
 * @param dtoClass                           the class of the DTO being mapped
 * @param ormTable                           the ORM table metadata
 * @param primaryKeyIndices                  the indices of the primary key columns in the row
 * @param primaryKeyTypes                    the types of the primary key columns
 * @param fieldMappings                      the mappings for the DTO fields
 * @param constructor                        the constructor to use for creating DTO instances
 * @param defaultConstructorUsed             whether the default constructor is used
 * @param canonicalConstructorFieldAccessors the field accessors for the canonical constructor
 * @param joinPlans                          the mapping plans for joined DTOs
 * @param nestedPlans                        the mapping plans for nested DTOs
 * @param oneToManyMappings                  the mappings for one-to-many relationships
 * @param manyToManyMappings                 the mappings for many-to-many relationships
 */
public record MappingPlan(Class<?> dtoClass,
                          OrmTable ormTable,
                          int[] primaryKeyIndices,
                          Class<?>[] primaryKeyTypes,
                          FieldMapping[] fieldMappings,
                          MethodHandle constructor,
                          boolean defaultConstructorUsed,
                          List<FieldAccessor> canonicalConstructorFieldAccessors,
                          Map<FieldAccessor, MappingPlan> joinPlans,
                          Map<FieldAccessor, MappingPlan> nestedPlans,
                          List<MappedOneToMany> oneToManyMappings,
                          List<MappedManyToMany> manyToManyMappings) {

    public record FieldMapping(int index,
                               FieldAccessor accessor,
                               Class<?> fieldType,
                               boolean isRelatedDto,
                               boolean isNestedDto,
                               @Nullable FieldAccessor relatedPkAccessor) {
    }
}
