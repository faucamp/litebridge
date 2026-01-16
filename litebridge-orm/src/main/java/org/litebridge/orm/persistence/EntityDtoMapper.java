package org.litebridge.orm.persistence;

import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;
import org.litebridge.tracking.FieldAccessorChain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityDtoMapper<DTO> {

    private final Class<DTO> dtoClass;
    private final List<Class<?>> entityClasses;
    private final Map<FieldAccessor, Map<Class<?>, FieldAccessor>> dtoToEntityFieldMap;
    private final Map<Class<?>, Map<FieldAccessor, FieldAccessor>> entityToDtoFieldMap;

    public EntityDtoMapper(final Class<DTO> dtoClass, final List<DtoEntityMapping> dtoEntityMappings) {
        this.dtoClass = dtoClass;

        final List<Class<?>> entityClasses = new ArrayList<>();
        final Map<FieldAccessor, Map<Class<?>, FieldAccessor>> dtoToEntityFieldMap = new HashMap<>();
        final Map<Class<?>, Map<FieldAccessor, FieldAccessor>> entityToDtoFieldMap = new HashMap<>();

        for (DtoEntityMapping dtoEntityMapping : dtoEntityMappings) {
            entityClasses.add(dtoEntityMapping.entityClass());

            dtoEntityMapping.dtoEntityFieldMap().forEach((dtoField, entityField) -> {
                final FieldAccessor dtoFieldAccessor = DtoIntrospector.fieldAccessor(dtoClass, dtoField);
                final FieldAccessor entityFieldAccessor = DtoIntrospector.fieldAccessor(dtoEntityMapping.entityClass(), entityField);

                dtoToEntityFieldMap.computeIfAbsent(dtoFieldAccessor, k -> new HashMap<>())
                        .put(dtoEntityMapping.entityClass(), entityFieldAccessor);

                entityToDtoFieldMap.computeIfAbsent(dtoEntityMapping.entityClass(), k -> new HashMap<>())
                        .put(entityFieldAccessor, dtoFieldAccessor);
            });
        }

        this.entityClasses = Collections.unmodifiableList(entityClasses);
        this.dtoToEntityFieldMap = Collections.unmodifiableMap(dtoToEntityFieldMap);
        this.entityToDtoFieldMap = Collections.unmodifiableMap(entityToDtoFieldMap);
    }

    public Class<DTO> dtoClass() {
        return dtoClass;
    }

    public List<Class<?>> entityClasses() {
        return entityClasses;
    }

    public List<Object> entities(final DTO dto) {
        ObjectUtils.requireNonNull(dto, "DTO cannot be null");
        final Map<Class<?>, Object> constructedEntities = new HashMap<>();
        final Map<FieldAccessor, FieldAccessor> postProcessQueue = new HashMap<>();

        entityToDtoFieldMap.forEach((entityClass, dtoFieldMap) -> {
            final Object entity = constructedEntities.computeIfAbsent(entityClass, ClassUtils::newInstance);

            dtoFieldMap.forEach((entityField, dtoField) -> {
                final Object value = dtoField.get(dto);
                entityField.set(entity, value);

                if (entityField instanceof FieldAccessorChain entityFieldAccessorChain) {
                    // Merge the intermediate entities with the constructed entity set
                    entityFieldAccessorChain.fieldAccessors()
                            .forEach(intermediateFieldAccessor -> {
                                if (entityFieldAccessorChain.isLast(intermediateFieldAccessor)) {
                                    return;
                                }

                                //TODO: support for multiple intermediate DTOs of the same type, and the interplay between different relationships of these types
                                final Object constructedIntermediateEntity = constructedEntities.get(intermediateFieldAccessor.type());
                                final Object intermediateEntity = ObjectUtils.requireNonNull(intermediateFieldAccessor.get(entity),
                                        () -> new IllegalStateException("Intermediate entity not constructed for field '%s'".formatted(intermediateFieldAccessor.name())));

                                if (constructedIntermediateEntity == null) {
                                    // Intermediate entity not constructed yet, so add the intermediate one as the current value base
                                    constructedEntities.put(intermediateFieldAccessor.type(), intermediateEntity);
                                } else {
                                    // Merge the intermediate entity with the constructed entity
                                    ClassFieldAccessorCache.fieldAccessors(intermediateFieldAccessor.type()).forEach(fieldAccessor -> {
                                                if (fieldAccessor.get(constructedIntermediateEntity) == null) {
                                                    final Object intermediateFieldValue = fieldAccessor.get(intermediateEntity);

                                                    if (intermediateFieldValue != null) {
                                                        fieldAccessor.set(constructedIntermediateEntity, intermediateFieldValue);
                                                    }
                                                }
                                            }
                                    );

                                    // Overwrite the intermediate entity with the constructed one
                                    intermediateFieldAccessor.set(entity, constructedIntermediateEntity);
                                }
                            });
                }
            });
        });

        return constructedEntities.values().stream().toList();
    }

    public DTO dto(final Object... entities) {
        return dto(List.of(entities));
    }

    public DTO dto(final List<Object> entities) {
        ObjectUtils.requireNonNull(entities, "Entities cannot be null");
        final DTO dto = ClassUtils.newInstance(dtoClass);

        entities.forEach(entity -> {
            entityToDtoFieldMap.get(entity.getClass()).forEach((entityField, dtoField) -> {
                if (dtoField.type() != entityField.type() && ClassFieldAccessorCache.isNestedDtoField(entityField.dtoClass(), entityField)) {
                    // Nested entity - get the PK and add that to the DTO field
                    final Object nestedDto = entityField.get(entity);
                    return;
                }

                dtoField.set(dto, entityField.get(entity));
            });
        });

        return dto;
    }
}
