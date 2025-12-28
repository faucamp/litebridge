package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;

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
}
