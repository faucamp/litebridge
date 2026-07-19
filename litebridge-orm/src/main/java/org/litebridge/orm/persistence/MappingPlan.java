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
        Map<FieldAccessor, MappingPlan> nestedPlans
) {
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
