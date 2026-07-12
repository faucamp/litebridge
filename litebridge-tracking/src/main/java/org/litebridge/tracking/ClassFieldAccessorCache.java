package org.litebridge.tracking;

import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.commons.StringUtils;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A utility class that facilitates caching and retrieving {@link FieldAccessor} instances for DTO classes.
 * <p>
 * The cache is organized by class type and field name for efficient lookup and reuse of {@link FieldAccessor} instances.
 */
public class ClassFieldAccessorCache {

    /**
     * Map of class -> field name -> field accessor
     */
    private final Map<Class<?>, Map<String, FieldAccessor>> classFieldAccessors = new ConcurrentHashMap<>();
    private final Map<Type, Class<?>[]> genericTypesMap = new ConcurrentHashMap<>();
    private final MethodHandles.Lookup lookup;
    private final Map<Class<?>, MethodHandles.Lookup> elevatedLookups = new ConcurrentHashMap<>();

    public ClassFieldAccessorCache() {
        this(MethodHandles.lookup());
    }

    public ClassFieldAccessorCache(final MethodHandles.Lookup lookup) {
        this.lookup = lookup;
    }

    public void registerElevatedLookup(final Class<?> dtoClass, final MethodHandles.Lookup elevatedLookup) {
        this.elevatedLookups.put(dtoClass, elevatedLookup);
    }

    public FieldAccessor fieldAccessorOrThrow(final Class<?> dtoClass, final String field) {
        if (field.indexOf('.') != -1) {
            final String[] subFieldAndRestOfPath = StringUtils.splitOnce(field, '.');
            final FieldAccessor subFieldAccessor = fieldAccessor(dtoClass, subFieldAndRestOfPath[0]);
            return chain(new FieldAccessorChain(subFieldAccessor, field, this), subFieldAndRestOfPath[1]);
        } else {
            final FieldAccessor fieldAccessor = ensureFieldAccessors(dtoClass).get(field);

            if (fieldAccessor == null) {
                throw new IllegalArgumentException("No field accessor found for field '" + field + "' in class " + dtoClass.getName());
            }

            return fieldAccessor;
        }
    }

    public List<FieldAccessor> fieldAccessors(final Class<?> dtoClass) {
        if (classFieldAccessors.containsKey(dtoClass)) {
            return classFieldAccessors.get(dtoClass).values().stream().toList();
        } else {
            final Map<String, FieldAccessor> fieldAccessors = ensureFieldAccessors(dtoClass);
            return fieldAccessors.values().stream().toList();
        }
    }

    public boolean isNestedDtoField(final Class<?> dtoClass, final FieldAccessor field) {
        if (field.dtoClass() != dtoClass
                || ClassUtils.isBasicType(field.type())
                || Collection.class.isAssignableFrom(field.type())
                || Map.class.isAssignableFrom(field.type())) {
            return false;
        }

        return ensureFieldAccessors(dtoClass).containsKey(field.name());
    }

    public FieldAccessor fieldAccessor(final Class<?> dtoClass, final String fieldName) {
        if (fieldName.indexOf('.') != -1) {
            // Nested field specification - traverse the field/property path
            final String[] subFieldAndRestOfPath = StringUtils.splitOnce(fieldName, '.');
            final FieldAccessor subFieldAccessor = fieldAccessor(dtoClass, subFieldAndRestOfPath[0]);
            return chain(new FieldAccessorChain(subFieldAccessor, fieldName, this), subFieldAndRestOfPath[1]);
        } else {
            return ObjectUtils.requireNonNull(ensureFieldAccessors(dtoClass).get(fieldName), () -> new IllegalArgumentException("Field '%s' not found in class '%s'".formatted(fieldName, dtoClass.getName())));
        }
    }

    private FieldAccessorChain chain(final FieldAccessorChain fieldAccessorChain, final String fieldPath) {
        final FieldAccessor subFieldAccessor;

        if (fieldPath.indexOf('.') != -1) {
            // Nested field specification - traverse the field/property path
            final String[] subFieldAndRestOfPath = StringUtils.splitOnce(fieldPath, '.');
            subFieldAccessor = fieldAccessor(fieldAccessorChain.type(), subFieldAndRestOfPath[0]);
            return chain(fieldAccessorChain.add(subFieldAccessor), subFieldAndRestOfPath[1]);
        } else {
            // Final field in the chain
            subFieldAccessor = fieldAccessor(fieldAccessorChain.type(), fieldPath);
            return fieldAccessorChain.add(subFieldAccessor);
        }
    }

    public FieldAccessor propertyAccessor(final Class<?> dtoClass, final String propertyName) {
        return ensureFieldAccessors(dtoClass).get(propertyName);
    }

    private Map<String, FieldAccessor> ensureFieldAccessors(final Class<?> dtoClass) {
        return classFieldAccessors.computeIfAbsent(dtoClass, this::createFieldAccessors);
    }

    private Map<String, FieldAccessor> createFieldAccessors(final Class<?> dtoClass) {
        final List<Field> fields = ClassUtils.getAllFields(dtoClass, lookup);

        return fields.stream()
                .map(field -> {
                    final MethodHandles.Lookup declaringClassLookup;

                    final Class<?> declaringClass = field.getDeclaringClass();
                    final MethodHandles.Lookup elevatedLookup = elevatedLookups.get(declaringClass);

                    if (elevatedLookup != null && (elevatedLookup.lookupModes() & MethodHandles.Lookup.PRIVATE) != 0) {
                        declaringClassLookup = elevatedLookup;
                    } else {
                        try {
                            declaringClassLookup = MethodHandles.privateLookupIn(declaringClass, lookup);
                        } catch (IllegalAccessException ex) {
                            throw new IllegalArgumentException(
                                    "Cannot create private lookup for declaring class: " + declaringClass.getName() +
                                            " while building accessors for DTO: " + dtoClass.getName() +
                                            ". Ensure the module is open to " + lookup.lookupClass().getModule().getName() + " or use register(Lookup, Class, TableSpec)",
                                    ex
                            );
                        }
                    }

                    return new DirectFieldAccessor(field, declaringClassLookup);
                })
                .collect(Collectors.toMap(FieldAccessor::name, Function.identity()));
    }

    public Class<?>[] getGenericTypes(final Field field) {
        return getGenericTypes(field.getGenericType());
    }

    private Class<?>[] getGenericTypes(final Type genericType) {
        return genericTypesMap.computeIfAbsent(genericType, ClassUtils::getGenericTypes);
    }
}
