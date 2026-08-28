package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.tracking.FieldAccessor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A utility class responsible for constructing Data Transfer Object (DTO) instances
 * using default or canonical constructors. This class leverages cached constructor data
 * for performance optimization and ensures proper mapping of field values to parameters.
 * It supports both Java POJOs and Java Records as DTOs.
 * <p>
 * The class heavily relies on a {@link TableRegistry} instance to retrieve metadata
 * about the DTOs being constructed, and it computes or caches constructor and field
 * accessor mappings as needed.
 * <p>
 * The caching mechanism ensures that constructors and their associated parameters
 * are resolved only once for each DTO class, significantly improving performance
 * for repeated instantiations.
 * <p>
 * Thread-safety is ensured via the use of {@link ConcurrentHashMap} for storing
 * cached constructor and field accessor data.
 */
public final class DtoConstructor {

    static final Object NO_CONSTRUCTOR = new Object();

    private final TableRegistry tableRegistry;
    private final Map<Class<?>, Object> defaultConstructorHandleCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> canonicalConstructorHandleCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<FieldAccessor>> canonicalConstructorFieldAccessorCache = new ConcurrentHashMap<>();

    /**
     * Creates a new DTO constructor instance.
     *
     * @param tableRegistry the table registry to use for dependency resolution
     */
    public DtoConstructor(final TableRegistry tableRegistry) {
        this.tableRegistry = tableRegistry;
    }

    public MappingInfo getMappingInfo(final Class<?> dtoClass) {
        cacheConstructors(dtoClass, null);
        final Optional<MethodHandle> defaultHandle = defaultConstructor(dtoClass);

        if (defaultHandle.isPresent()) {
            return new MappingInfo(defaultHandle.get(), true, Collections.emptyList());
        }
        
        final MethodHandle canonicalHandle = canonicalConstructor(dtoClass)
                .orElseThrow(() -> new IllegalArgumentException("No suitable constructor found for DTO class: " + dtoClass));
        return new MappingInfo(canonicalHandle, false, canonicalConstructorFieldAccessorCache.get(dtoClass));
    }

    /**
     * Creates a new instance of the specified DTO class using the provided field accessor values
     * to resolve constructor arguments.
     *
     * @param <DTO>               The type of the Data Transfer Object to instantiate.
     * @param dtoClass            The class of the DTO to be instantiated.
     * @param fieldAccessorValues A list of {@code FieldAccessorValue} objects, where each object
     *                            specifies a field accessor and its corresponding value for the DTO.
     * @return A {@code ConstructionResult<DTO>} containing the instantiated DTO object and a
     * boolean indicating whether the default constructor was used.
     * @throws IllegalArgumentException If no suitable constructor is found for the given DTO class.
     */
    @SuppressWarnings("unchecked")
    public <DTO> ConstructionResult<DTO> newInstance(final Class<DTO> dtoClass, final List<FieldAccessorValue> fieldAccessorValues) {
        cacheConstructors(dtoClass, null);
        return defaultConstructor(dtoClass)
                .map(handle -> {
                    try {
                        return new ConstructionResult<>((DTO) handle.invoke(), true);
                    } catch (Throwable e) {
                        throw new RuntimeException("Failed to instantiate DTO: " + dtoClass, e);
                    }
                })
                .orElseGet(() -> {
                    final MethodHandle handle = canonicalConstructor(dtoClass)
                            .orElseThrow(() -> new IllegalArgumentException("No suitable constructor found for DTO class: " + dtoClass));

                    final Map<FieldAccessor, @Nullable Object> valuesByField = new HashMap<>(fieldAccessorValues.size());

                    for (FieldAccessorValue fieldAccessorValue : fieldAccessorValues) {
                        final Object value = fieldAccessorValue.value();

                        if (value instanceof DtoDependency) {
                            valuesByField.put(fieldAccessorValue.field(), null);
                        } else {
                            valuesByField.put(fieldAccessorValue.field(), value);
                        }
                    }

                    final List<FieldAccessor> canonicalConstructorFieldAccessors = canonicalConstructorFieldAccessorCache.get(dtoClass);
                    final @Nullable Object[] args = new Object[canonicalConstructorFieldAccessors.size()];

                    for (int i = 0; i < canonicalConstructorFieldAccessors.size(); i++) {
                        final FieldAccessor fieldAccessor = canonicalConstructorFieldAccessors.get(i);
                        args[i] = valuesByField.get(fieldAccessor);
                    }

                    try {
                        return new ConstructionResult<>((DTO) handle.invokeWithArguments(args), false);
                    } catch (Throwable e) {
                        throw new RuntimeException("Failed to instantiate DTO: " + dtoClass, e);
                    }
                });
    }

    private Optional<MethodHandle> defaultConstructor(final Class<?> dtoClass) {
        final Object cachedHandle = defaultConstructorHandleCache.get(dtoClass);

        if (cachedHandle == NO_CONSTRUCTOR) {
            return Optional.empty();
        }

        return Optional.of((MethodHandle) cachedHandle);
    }

    private Optional<MethodHandle> canonicalConstructor(final Class<?> dtoClass) {
        final Object cachedHandle = canonicalConstructorHandleCache.get(dtoClass);

        if (cachedHandle == NO_CONSTRUCTOR) {
            return Optional.empty();
        }

        return Optional.of((MethodHandle) cachedHandle);
    }

    private <DTO> void cacheConstructors(final Class<DTO> dtoClass, final @Nullable Class<?> parentDtoClass) {
        if (defaultConstructorHandleCache.containsKey(dtoClass)) {
            return;
        }

        final OrmTable ormTable;

        if (parentDtoClass != null) {
            ormTable = tableRegistry.getTableInContext(dtoClass, parentDtoClass)
                    .orElseGet(() -> tableRegistry.getOrmTableOrThrow(dtoClass));
        } else {
            ormTable = tableRegistry.getOrmTableOrThrow(dtoClass);
        }

        final List<FieldAccessor> fieldAccessors = ormTable.fieldAcessorStream().toList();
        final Set<Class<?>> fieldAccessorTypes = new HashSet<>(fieldAccessors.size());
        final boolean matchParameterNames = fieldAccessors.stream().anyMatch(fieldAccessor -> !fieldAccessorTypes.add(fieldAccessor.type()));

        final RecordComponent[] recordComponents = dtoClass.getRecordComponents();
        final Constructor<DTO>[] constructors = ClassUtils.getConstructors(dtoClass);

        Constructor<DTO> defaultConstructor = null;
        Constructor<DTO> canonicalConstructor = null;
        List<FieldAccessor> canonicalConstructorFieldAccessors = null;

        for (Constructor<DTO> constructor : constructors) {
            final int constructorParameterCount = constructor.getParameterCount();

            if (constructorParameterCount == 0) {
                defaultConstructor = constructor;
            } else if (recordComponents != null && recordComponents.length == constructorParameterCount) {
                // Constructing a record
                boolean match = true;
                final List<FieldAccessor> mappedFieldAccessors = new ArrayList<>();
                final Map<String, FieldAccessor> unmappedFieldAccessors = fieldAccessors.stream()
                        .collect(Collectors.toMap(
                                FieldAccessor::name,
                                Function.identity(),
                                (fa1, fa2) -> fa1,
                                HashMap::new
                        ));

                for (int i = 0; i < recordComponents.length; i++) {
                    final RecordComponent recordComponent = recordComponents[i];
                    final Class<?> parameterType = recordComponent.getType();
                    final Parameter parameter = constructor.getParameters()[i];
                    final FieldAccessor fieldAccessor = unmappedFieldAccessors.get(parameter.getName());

                    if (fieldAccessor == null) {
                        match = false;
                        break;
                    }

                    if (!parameterType.isAssignableFrom(fieldAccessor.type())) {
                        match = false;
                        break;
                    }

                    if (!parameter.getName().equals(fieldAccessor.name())) {
                        match = false;
                        break;
                    }

                    mappedFieldAccessors.add(fieldAccessor);
                    unmappedFieldAccessors.remove(fieldAccessor.name());
                }

                if (match) {
                    canonicalConstructor = constructor;
                    canonicalConstructorFieldAccessors = mappedFieldAccessors;
                }

            } else if (fieldAccessors.size() == constructorParameterCount) {
                // POJO constructor
                final List<FieldAccessor> mappedFieldAccessors = new ArrayList<>();

                if (matchParameterNames) {
                    final Map<String, FieldAccessor> unmappedFieldAccessors = fieldAccessors.stream()
                            .collect(Collectors.toMap(
                                    FieldAccessor::name,
                                    Function.identity(),
                                    (fa1, fa2) -> fa1,
                                    HashMap::new
                            ));

                    for (Parameter parameter : constructor.getParameters()) {
                        if (!parameter.isNamePresent()) {
                            throw new IllegalStateException("Unable to determine parameter names for canonical constructor (code not compiled with '-parameters' flag); since there are multiple parameters of the same type, additional mapping config is required");
                        }

                        final FieldAccessor fieldAccessor = unmappedFieldAccessors.get(parameter.getName());

                        if (fieldAccessor == null) {
                            break;
                        }

                        mappedFieldAccessors.add(fieldAccessor);
                        unmappedFieldAccessors.remove(fieldAccessor.name());
                    }
                } else {
                    // Match parameter types only
                    for (Parameter parameter : constructor.getParameters()) {
                        final FieldAccessor fieldAccessor = fieldAccessors.stream()
                                .filter(fa -> fa.type().equals(parameter.getType()))
                                .findFirst().orElse(null);

                        if (fieldAccessor == null) {
                            break;
                        }

                        mappedFieldAccessors.add(fieldAccessor);
                    }
                }

                if (mappedFieldAccessors.size() == constructorParameterCount) {
                    canonicalConstructor = constructor;
                    canonicalConstructorFieldAccessors = mappedFieldAccessors;
                }
            }
        }

        if (defaultConstructor == null && canonicalConstructor == null) {
            throw new IllegalArgumentException("No suitable constructor found for DTO class: " + dtoClass);
        }

        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            if (defaultConstructor != null) {
                defaultConstructor.setAccessible(true);
            }
            if (canonicalConstructor != null) {
                canonicalConstructor.setAccessible(true);
            }
            defaultConstructorHandleCache.put(dtoClass, defaultConstructor != null ? lookup.unreflectConstructor(defaultConstructor) : NO_CONSTRUCTOR);
            canonicalConstructorHandleCache.put(dtoClass, canonicalConstructor != null ? lookup.unreflectConstructor(canonicalConstructor) : NO_CONSTRUCTOR);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to unreflect constructor for DTO class: " + dtoClass, e);
        }
        canonicalConstructorFieldAccessorCache.put(dtoClass, canonicalConstructorFieldAccessors != null ? canonicalConstructorFieldAccessors : Collections.emptyList());

        // Cache related DTO constructors
        ormTable.getRelatedDtoClasses().forEach(relatedDtoClass -> cacheConstructors(relatedDtoClass, dtoClass));
    }

    /**
     * The result of constructing a Data Transfer Object (DTO).
     * <p>
     * The record encapsulates the instantiated DTO and a flag indicating
     * whether the default constructor was used for the instantiation.
     *
     * @param <DTO>                  The type of the Data Transfer Object contained in the result.
     * @param dto                    The instance of the constructed Data Transfer Object.
     * @param defaultConstructorUsed A boolean flag indicating whether the default
     *                               constructor was used to create the DTO. Returns {@code true} if the default
     *                               constructor was used, otherwise {@code false}.
     */
    public record ConstructionResult<DTO>(DTO dto, boolean defaultConstructorUsed) {
    }

    /**
     * A pair consisting of a {@link FieldAccessor} and its corresponding value.
     * <p>
     * This record is primarily used for associating a specific field of a Data Transfer Object (DTO)
     * with its resolved value, often for the purpose of constructing DTOs or mapping data to their fields.
     * <p>
     * The {@code field} represents the metadata and manipulation methods for a given field or property
     * in a DTO, while the {@code value} refers to the value assigned to that field.
     *
     * @param field The {@code FieldAccessor} instance representing the field to be accessed or manipulated.
     *              Must not be {@code null} and provides field-level metadata and interaction capabilities.
     * @param value The value associated with the specified field. Can be {@code null} if the field allows null values.
     */
    public record FieldAccessorValue(FieldAccessor field, @Nullable Object value) {
    }

    /**
     * Dependency configuration for a Data Transfer Object (DTO).
     * <p>
     * This class encapsulates the relationships and dependencies between a field in a parent DTO
     * and the DTO class to which it is related. It also includes the information needed to
     * resolve the primary key values of the target DTO class.
     *
     * @param field            The {@code FieldAccessor} instance representing the field in the
     *                         parent DTO that references the target DTO class.
     * @param targetDtoClass   The {@code Class} object representing the target DTO type.
     * @param targetPrimaryKey A {@code List} of {@code FieldAccessorValue} objects corresponding
     *                         to the primary key fields of the target DTO class. Each entry
     *                         specifies a field accessor and its corresponding value.
     */
    public record DtoDependency(FieldAccessor field, Class<?> targetDtoClass,
                                List<FieldAccessorValue> targetPrimaryKey) {

        /**
         * Returns the primary key values of the target DTO.
         *
         * @return the target primary key values
         */
        public List<Object> targetPrimaryKeyValue() {
            return targetPrimaryKey.stream()
                    .map(FieldAccessorValue::value)
                    .filter(Objects::nonNull)
                    .toList();
        }
    }

    /**
     * Information about a particular constructor.
     *
     * @param constructor                        The constructor method handle
     * @param defaultConstructorUsed             Whether the consructor is a default (no arg) constructor
     * @param canonicalConstructorFieldAccessors The field accessors for the canonical constructor
     */
    public record MappingInfo(
            MethodHandle constructor,
            boolean defaultConstructorUsed,
            List<FieldAccessor> canonicalConstructorFieldAccessors
    ) {
    }
}
