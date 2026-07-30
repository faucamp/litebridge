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

    /**
     * Constructs a new {@code ClassFieldAccessorCache} using the default {@link MethodHandles#lookup()}.
     */
    public ClassFieldAccessorCache() {
        this(MethodHandles.lookup());
    }

    /**
     * Constructs a new {@code ClassFieldAccessorCache} using the provided {@link MethodHandles.Lookup}.
     *
     * @param lookup the lookup to use for access checking
     */
    public ClassFieldAccessorCache(final MethodHandles.Lookup lookup) {
        this.lookup = lookup;
    }

    /**
     * Registers an elevated {@link MethodHandles.Lookup} for a specific DTO class, allowing access to private fields.
     *
     * @param dtoClass       the class for which to register the lookup
     * @param elevatedLookup the elevated lookup instance
     */
    public void registerElevatedLookup(final Class<?> dtoClass, final MethodHandles.Lookup elevatedLookup) {
        this.elevatedLookups.put(dtoClass, elevatedLookup);
    }

    /**
     * Retrieves a {@link FieldAccessor} for the specified field path, or throws an exception if it doesn't exist.
     *
     * @param dtoClass the class containing the field
     * @param field    the field path (can be dot-separated for nested fields)
     * @return the field accessor
     * @throws IllegalArgumentException if the field cannot be found
     */
    public FieldAccessor fieldAccessorOrThrow(final Class<?> dtoClass, final String field) {
        final FieldAccessor fieldAccessor = fieldAccessorOrNull(dtoClass, field);

        if (fieldAccessor == null) {
            throw new IllegalArgumentException("No field accessor found for field '" + field + "' in class " + dtoClass.getName());
        }

        return fieldAccessor;
    }

    /**
     * Retrieves a {@link FieldAccessor} for the specified field path, or {@code null} if it doesn't exist.
     *
     * @param dtoClass the class containing the field
     * @param field    the field path (can be dot-separated for nested fields)
     * @return the field accessor, or {@code null} if not found
     */
    public @org.jspecify.annotations.Nullable FieldAccessor fieldAccessorOrNull(final Class<?> dtoClass, final String field) {
        if (field.indexOf('.') != -1) {
            final String[] subFieldAndRestOfPath = StringUtils.splitOnce(field, '.');
            final Map<String, FieldAccessor> accessors = ensureFieldAccessors(dtoClass);
            final FieldAccessor subFieldAccessor = accessors.get(subFieldAndRestOfPath[0]);
            if (subFieldAccessor == null) return null;
            return chain(new FieldAccessorChain(subFieldAccessor, field, this), subFieldAndRestOfPath[1]);
        } else {
            return ensureFieldAccessors(dtoClass).get(field);
        }
    }

    /**
     * Retrieves all field accessors for the specified class.
     *
     * @param dtoClass the class to retrieve accessors for
     * @return a list of all field accessors
     */
    public List<FieldAccessor> fieldAccessors(final Class<?> dtoClass) {
        if (classFieldAccessors.containsKey(dtoClass)) {
            return classFieldAccessors.get(dtoClass).values().stream().toList();
        } else {
            final Map<String, FieldAccessor> fieldAccessors = ensureFieldAccessors(dtoClass);
            return fieldAccessors.values().stream().toList();
        }
    }

    /**
     * Determines if a field represents a nested DTO.
     *
     * @param dtoClass the class containing the field
     * @param field    the field accessor to check
     * @return {@code true} if the field is a nested DTO; {@code false} otherwise
     */
    public boolean isNestedDtoField(final Class<?> dtoClass, final FieldAccessor field) {
        if (field.dtoClass() != dtoClass
                || ClassUtils.isBasicType(field.type())
                || Collection.class.isAssignableFrom(field.type())
                || Map.class.isAssignableFrom(field.type())) {
            return false;
        }

        return ensureFieldAccessors(dtoClass).containsKey(field.name());
    }

    /**
     * Retrieves a {@link FieldAccessor} for the specified field name or path.
     *
     * @param dtoClass  the class containing the field
     * @param fieldName the field name or path
     * @return the field accessor
     */
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

    /**
     * Retrieves a property accessor for the specified property name.
     *
     * @param dtoClass     the class containing the property
     * @param propertyName the name of the property
     * @return the field accessor for the property
     */
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

    /**
     * Retrieves the generic type arguments for a given field, with caching.
     *
     * @param field the field to inspect
     * @return an array of classes representing the generic type arguments
     */
    public Class<?>[] getGenericTypes(final Field field) {
        return getGenericTypes(field.getGenericType());
    }

    private Class<?>[] getGenericTypes(final Type genericType) {
        return genericTypesMap.computeIfAbsent(genericType, ClassUtils::getGenericTypes);
    }
}
