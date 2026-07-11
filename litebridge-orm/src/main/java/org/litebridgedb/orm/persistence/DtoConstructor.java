package org.litebridgedb.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.ClassUtils;
import org.litebridgedb.tracking.FieldAccessor;

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

public final class DtoConstructor {

    static final Object NO_CONSTRUCTOR = new Object();

    private final TableRegistry tableRegistry;
    private final Map<Class<?>, Object> defaultConstructorCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> canonicalConstructorCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<FieldAccessor>> canonicalConstructorFieldAccessorCache = new ConcurrentHashMap<>();

    public DtoConstructor(final TableRegistry tableRegistry) {
        this.tableRegistry = tableRegistry;
    }

    public <DTO> ConstructionResult<DTO> newInstance(final Class<DTO> dtoClass, final List<FieldAccessorValue> fieldAccessorValues) {
        cacheConstructors(dtoClass, null);
        return defaultConstructor(dtoClass)
                .map(dtoConstructor -> new ConstructionResult<>(ClassUtils.newInstance(dtoClass, dtoConstructor), true))
                .orElseGet(() -> {
                    final Constructor<DTO> dtoConstructor = canonicalConstructor(dtoClass, fieldAccessorValues)
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

                    return new ConstructionResult<>(ClassUtils.newInstance(dtoClass, dtoConstructor, args), false);
                });
    }

    @SuppressWarnings("unchecked")
    private <DTO> Optional<Constructor<DTO>> defaultConstructor(final Class<DTO> dtoClass) {
        final Object cachedConstructor = defaultConstructorCache.get(dtoClass);

        if (cachedConstructor == NO_CONSTRUCTOR) {
            return Optional.empty();
        }

        return Optional.of((Constructor<DTO>) cachedConstructor);
    }

    @SuppressWarnings("unchecked")
    private <DTO> Optional<Constructor<DTO>> canonicalConstructor(final Class<DTO> dtoClass, final List<FieldAccessorValue> fieldAccessorValues) {
        final Object cachedConstructor = canonicalConstructorCache.get(dtoClass);

        if (cachedConstructor == NO_CONSTRUCTOR) {
            return Optional.empty();
        }

        return Optional.of((Constructor<DTO>) cachedConstructor);
    }

    private <DTO> void cacheConstructors(final Class<DTO> dtoClass, final @Nullable Class<?> parentDtoClass) {
        if (defaultConstructorCache.containsKey(dtoClass)) {
            return;
        }

        final OrmTable ormTable;

        if (parentDtoClass != null) {
            ormTable = tableRegistry.getTableInContext(dtoClass, parentDtoClass)
                    .orElseGet(() -> tableRegistry.getTableOrThrow(dtoClass));
        } else {
            ormTable = tableRegistry.getTableOrThrow(dtoClass);
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

        defaultConstructorCache.put(dtoClass, defaultConstructor != null ? defaultConstructor : NO_CONSTRUCTOR);
        canonicalConstructorCache.put(dtoClass, canonicalConstructor != null ? canonicalConstructor : NO_CONSTRUCTOR);
        canonicalConstructorFieldAccessorCache.put(dtoClass, canonicalConstructorFieldAccessors != null ? canonicalConstructorFieldAccessors : Collections.emptyList());

        // Cache related DTO constructors
        ormTable.getRelatedDtoClasses().forEach(relatedDtoClass -> cacheConstructors(relatedDtoClass, dtoClass));
    }

    public record ConstructionResult<DTO>(DTO dto, boolean defaultConstructorUsed) {
    }

    public record FieldAccessorValue(FieldAccessor field, @Nullable Object value) {
    }

    public record DtoDependency(FieldAccessor field, Class<?> targetDtoClass,
                                List<FieldAccessorValue> targetPrimaryKey) {

        public List<Object> targetPrimaryKeyValue() {
            return targetPrimaryKey.stream()
                    .map(FieldAccessorValue::value)
                    .filter(Objects::nonNull)
                    .toList();
        }
    }
}
