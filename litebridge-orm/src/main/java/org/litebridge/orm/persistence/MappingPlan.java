package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.dto.DtoJoinSpec;
import org.litebridge.tracking.FieldAccessor;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;

/**
 * A "pre-compiled" mapping plan for a specific DTO class and query specification.
 * It pre-resolves column indices, constructors, and field mappers to minimize overhead during row processing.
 *
 * @param dtoClass                          the class of the DTO being mapped
 * @param ormTable                          the ORM table metadata
 * @param primaryKeyIndices                 the indices of the primary key columns in the row
 * @param primaryKeyTypes                   the types of the primary key columns
 * @param fieldMappings                    the mappings for the DTO fields
 * @param constructor                       the constructor to use for creating DTO instances
 * @param defaultConstructorUsed            whether the default constructor is used
 * @param canonicalConstructorFieldAccessors the field accessors for the canonical constructor
 * @param joinPlans                         the mapping plans for joined DTOs
 * @param nestedPlans                       the mapping plans for nested DTOs
 * @param oneToManyMappings                the mappings for one-to-many relationships
 * @param manyToManyMappings               the mappings for many-to-many relationships
 */
public record MappingPlan(
        Class<?> dtoClass,
        OrmTable ormTable,
        int[] primaryKeyIndices,
        Class<?>[] primaryKeyTypes,
        FieldMapping[] fieldMappings,
        MethodHandle constructor,
        boolean defaultConstructorUsed,
        List<FieldAccessor> canonicalConstructorFieldAccessors,
        Map<DtoJoinSpec, MappingPlan> joinPlans,
        Map<FieldAccessor, MappingPlan> nestedPlans,
        List<MappedOneToMany> oneToManyMappings,
        List<MappedManyToMany> manyToManyMappings
) {
    public java.util.List<Object> extractPk(org.litebridge.db.spi.Row row) {
        java.util.List<Object> pk = new java.util.ArrayList<>(primaryKeyIndices.length);
        boolean pkNull = true;
        for (int i = 0; i < primaryKeyIndices.length; i++) {
            int index = primaryKeyIndices[i];
            if (index != -1) {
                Object value = row.getValue(index);
                if (value != null) {
                    pkNull = false;
                    pk.add(value); // Type conversion will happen in toDto if needed, but for cache key raw is fine if consistent
                } else {
                    pk.add(null);
                }
            } else {
                pk.add(null);
            }
        }
        if (pkNull && primaryKeyIndices.length > 0 && !allIndicesMissing()) {
            return null;
        }
        if (pkNull) {
            pk.clear();
            pk.add(row.hashCode());
        }
        return pk;
    }

    private boolean allIndicesMissing() {
        for (int index : primaryKeyIndices) {
            if (index != -1) return false;
        }
        return true;
    }

    public record FieldMapping(
            int index,
            FieldAccessor accessor,
            Class<?> fieldType,
            boolean isRelatedDto,
            boolean isNestedDto,
            @Nullable FieldAccessor relatedPkAccessor
    ) {
    }
}
