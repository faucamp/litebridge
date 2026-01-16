package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Direct field access {@code FieldAccessor} implementation.
 * <p>
 * Provides functionality for accessing and manipulating fields of a data transfer object (DTO) directly
 * using Java Reflection.
 * <p>
 * This class enables retrieving field names, types, generic types, and the declaring class.
 * It also supports getting and setting field values within a given DTO instance.
 */
public class FieldAccessorImpl implements FieldAccessor {

    private final Field field;

    public FieldAccessorImpl(final Field field) {
        field.setAccessible(true);
        this.field = field;
    }

    @Override
    public String name() {
        return field.getName();
    }

    @Override
    public Object get(final Object dto) {
        try {
            return field.get(dto);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Failed to get field '%s' of DTO: %s".formatted(field.getName(), dto), ex);
        }
    }

    @Override
    public void set(final Object dto, final @Nullable Object value) {
        try {
            field.set(dto, value);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Failed to set field '%s' of DTO: %s".formatted(field.getName(), dto), ex);
        }
    }

    @Override
    public Class<?> type() {
        return field.getType();
    }

    @Override
    public Class<?>[] genericTypes() {
        return ClassFieldCache.getGenericTypes(field);
    }

    @Override
    public Class<?> dtoClass() {
        return field.getDeclaringClass();
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final FieldAccessorImpl that)) return false;
        return Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(field);
    }
}
