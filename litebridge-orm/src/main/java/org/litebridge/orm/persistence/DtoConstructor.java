package org.litebridge.orm.persistence;

import org.litebridge.commons.ClassUtils;
import org.litebridge.tracking.FieldAccessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class DtoConstructor {

    static final Object NO_CONSTRUCTOR = new Object();
    private static final Map<Class<?>, Object> defaultConstructorCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Object> canonicalConstructorCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<FieldAccessor>> canonicalConstructorFieldAccessorCache = new ConcurrentHashMap<>();

    private DtoConstructor() {
    }

    public static <DTO> ConstructionResult<DTO> newInstance(final Class<DTO> dtoClass, final List<FieldAccessorValue> fieldAccessorValues) {
        cacheConstructors(dtoClass, fieldAccessorValues);
        return defaultConstructor(dtoClass)
                .map(dtoConstructor -> new ConstructionResult<>(ClassUtils.newInstance(dtoClass, dtoConstructor), true))
                .orElseGet(() -> {
                    final Constructor<DTO> dtoConstructor = canonicalConstructor(dtoClass, fieldAccessorValues)
                            .orElseThrow(() -> new IllegalArgumentException("No suitable constructor found for DTO class: " + dtoClass));

                    final Object[] args = canonicalConstructorFieldAccessorCache.get(dtoClass).stream()
                            .map(fieldAccessor -> fieldAccessorValues.stream()
                                    .filter(value -> value.field() == fieldAccessor)
                                    .map(FieldAccessorValue::value)
                                    .findFirst()
                                    .orElseThrow())
                            .toArray();

                    return new ConstructionResult<>(ClassUtils.newInstance(dtoClass, dtoConstructor, args), false);
                });
    }

    @SuppressWarnings("unchecked")
    private static <DTO> Optional<Constructor<DTO>> defaultConstructor(final Class<DTO> dtoClass) {
        final Object cachedConstructor = defaultConstructorCache.get(dtoClass);

        if (cachedConstructor == NO_CONSTRUCTOR) {
            return Optional.empty();
        }

        return Optional.of((Constructor<DTO>) cachedConstructor);
    }

    @SuppressWarnings("unchecked")
    private static <DTO> Optional<Constructor<DTO>> canonicalConstructor(final Class<DTO> dtoClass, final List<FieldAccessorValue> fieldAccessorValues) {
        final Object cachedConstructor = canonicalConstructorCache.get(dtoClass);

        if (cachedConstructor == NO_CONSTRUCTOR) {
            return Optional.empty();
        }

        return Optional.of((Constructor<DTO>) cachedConstructor);
    }

    private static <DTO> void cacheConstructors(final Class<DTO> dtoClass, final List<FieldAccessorValue> fieldAccessorValues) {
        if (defaultConstructorCache.containsKey(dtoClass)) {
            return;
        }

        final RecordComponent[] recordComponents = dtoClass.getRecordComponents();
        final Constructor<DTO>[] constructors = ClassUtils.getConstructors(dtoClass);

        Constructor<DTO> defaultConstructor = null;
        Constructor<DTO> canonicalConstructor = null;
        List<FieldAccessor> canonicalConstructorFieldAccessors = null;

        for (Constructor<DTO> constructor : constructors) {
            final int constructorParameterCount = constructor.getParameterCount();

            if (constructorParameterCount == 0) {
                defaultConstructor = constructor;
            } else if (recordComponents != null) {
                if (recordComponents.length == constructorParameterCount) {
                    boolean match = true;

                    for (int i = 0; i < recordComponents.length; i++) {
                        final RecordComponent recordComponent = recordComponents[i];
                        final Class<?> parameterType = recordComponent.getType();
                        final Class<?> constructorParameterType = constructor.getParameterTypes()[i];

                        if (!parameterType.isAssignableFrom(constructorParameterType)) {
                            match = false;
                            break;
                        }
                    }

                    if (match) {
                        canonicalConstructor = constructor;
                        canonicalConstructorFieldAccessors = fieldAccessorValues.stream().map(FieldAccessorValue::field).toList();
                    }
                }
            } else if (fieldAccessorValues.size() == constructorParameterCount) {
                final List<FieldAccessor> unmappedFieldAccessors = fieldAccessorValues.stream()
                        .map(FieldAccessorValue::field)
                        .collect(Collectors.toCollection(ArrayList::new));
                final List<FieldAccessor> mappedFieldAccessors = new ArrayList<>();

                for (Parameter parameter : constructor.getParameters()) {
                    final FieldAccessor fieldAccessor = unmappedFieldAccessors.stream()
                            .filter(field -> field.type() == parameter.getType())
                            .findFirst().orElse(null);

                    if (fieldAccessor == null) {
                        break;
                    }

                    mappedFieldAccessors.add(fieldAccessor);
                    unmappedFieldAccessors.remove(fieldAccessor);
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
    }

    public record ConstructionResult<DTO>(DTO dto, boolean defaultConstructorUsed) {
    }

    public record FieldAccessorValue(FieldAccessor field, Object value) {
    }
}
