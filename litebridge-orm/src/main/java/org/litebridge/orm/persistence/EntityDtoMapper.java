package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.orm.api.spec.FieldSpec;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;
import org.litebridge.tracking.FieldAccessorChain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Mapper for converting between complex Data Transfer Objects (DTOs) and their corresponding "entity" objects.
 * Provides functionality for converting a DTO to a list of entities and vice versa, based on a set of field mappings
 * specified via {@link DtoEntityMapping}.
 *
 * @param <DTO> the type parameter of the Data Transfer Object (DTO)
 */
public class EntityDtoMapper<DTO> {

    private final Class<DTO> dtoClass;
    private final List<Class<?>> entityClasses;
    private final Map<Class<?>, Map<FieldAccessor, FieldAccessor>> entityToDtoFieldMap;
    private final ClassFieldAccessorCache classFieldAccessorCache;

    public EntityDtoMapper(final Class<DTO> dtoClass, final List<DtoEntityMapping> dtoEntityMappings, final ClassFieldAccessorCache classFieldAccessorCache) {
        this.dtoClass = dtoClass;
        this.classFieldAccessorCache = classFieldAccessorCache;

        final List<Class<?>> entityClasses = new ArrayList<>();
        final Map<Class<?>, Map<FieldAccessor, FieldAccessor>> entityToDtoFieldMap = new HashMap<>();

        for (DtoEntityMapping dtoEntityMapping : dtoEntityMappings) {
            entityClasses.add(dtoEntityMapping.entityClass());

            dtoEntityMapping.dtoEntityFieldMap().forEach((dtoField, entityField) -> {
                final FieldAccessor dtoFieldAccessor = fieldAccessor(dtoClass, dtoField);
                final FieldAccessor entityFieldAccessor = fieldAccessor(dtoEntityMapping.entityClass(), entityField);

                entityToDtoFieldMap.computeIfAbsent(dtoEntityMapping.entityClass(), k -> new HashMap<>())
                        .put(entityFieldAccessor, dtoFieldAccessor);
            });
        }

        this.entityClasses = Collections.unmodifiableList(entityClasses);
        this.entityToDtoFieldMap = Collections.unmodifiableMap(entityToDtoFieldMap);
    }

    public Class<DTO> dtoClass() {
        return dtoClass;
    }

    public List<Class<?>> entityClasses() {
        return entityClasses;
    }

    /**
     * Converts the given DTO into a list of entity objects based on the mappings defined in the class.
     * The entities are constructed and populated with field values fetched from the DTO.
     * This method ensures that composite relationships and intermediate entities are properly handled.
     *
     * @param dto the data transfer object (DTO) to be converted into entities; must not be null
     * @return a list of constructed and populated entity objects
     * @throws IllegalArgumentException if {@code dto} is null
     */
    public List<Object> entities(final DTO dto) {
        Objects.requireNonNull(dto, "DTO cannot be null");
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
                                    classFieldAccessorCache.fieldAccessors(intermediateFieldAccessor.type()).forEach(fieldAccessor -> {
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

    /**
     * Converts the specified entity objects into a Data Transfer Object (DTO).
     * Maps the fields of the entities into the corresponding fields of the DTO
     * based on the predefined entity-to-DTO field mappings. Also handles nested
     * DTO fields if present.
     *
     * @param entities the array of entity objects to be converted; must not be null
     * @return a DTO populated with field values extracted from the given entities
     * @throws IllegalArgumentException if {@code entities} is null
     */
    public DTO dto(final Object... entities) {
        return dto(List.of(entities));
    }

    /**
     * Converts a list of entity objects into a Data Transfer Object (DTO).
     * Maps the fields of the entities into the corresponding fields of the DTO
     * based on the predefined entity-to-DTO field mappings. Also handles nested
     * DTO fields if present.
     *
     * @param entities the list of entity objects to be converted; must not be null
     * @return a DTO populated with field values extracted from the given entities
     * @throws IllegalArgumentException if {@code entities} is null
     */
    public DTO dto(final List<Object> entities) {
        Objects.requireNonNull(entities, "Entities cannot be null");
        final DTO dto = ClassUtils.newInstance(dtoClass);

        entities.forEach(entity -> {
            entityToDtoFieldMap.get(entity.getClass()).forEach((entityField, dtoField) -> {
                if (dtoField.type() != entityField.type() && classFieldAccessorCache.isNestedDtoField(entityField.dtoClass(), entityField)) {
                    // Nested entity - get the PK and add that to the DTO field
                    final Object nestedDto = entityField.get(entity);
                    return;
                }

                // Composite DTO fields may be mapped to multiple entities via nested DTOs (e.g. one-to-many relationships).
                // The combination of differently-selected DTOs in this combination may cause set values to be overridden with nulls (such as when a JOIN was left out but still mapped here)
                if (isFieldSet(dtoField, dtoField.get(dto))) {
                    dtoField.set(dto, entityField.get(entity));
                }
            });
        });

        return dto;
    }

    private static boolean isFieldSet(final FieldAccessor field, final @Nullable Object value) {
        if (value == null) {
            return true;
        }

        final Class<?> fieldType = field.type();

        if (fieldType.isPrimitive()) {
            if (Boolean.TYPE == fieldType) {
                return !((boolean) value);
            } else if (Character.TYPE == fieldType
                    || Byte.TYPE == fieldType
                    || Short.TYPE == fieldType
                    || Integer.TYPE == fieldType
                    || Long.TYPE == fieldType
                    || Float.TYPE == fieldType
                    || Double.TYPE == fieldType) {
                return value.equals(0);
            }
        }

        return false;
    }

    private FieldAccessor fieldAccessor(final Class<?> dtoClass, final FieldSpec fieldSpec) {
        if (fieldSpec.property()) {
            return classFieldAccessorCache.propertyAccessor(dtoClass, fieldSpec.name());
        } else {
            return classFieldAccessorCache.fieldAccessor(dtoClass, fieldSpec.name());
        }
    }
}
