package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;

import java.beans.PropertyDescriptor;

public class PropertyAccessor implements FieldAccessor {

    private final PropertyDescriptor propertyDescriptor;

    public PropertyAccessor(final PropertyDescriptor propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
    }

    @Override
    public String name() {
        return propertyDescriptor.getName();
    }

    @Override
    public Object get(final Object dto) {
        try {
            return propertyDescriptor.getReadMethod().invoke(dto);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to get property '%s' of DTO: %s".formatted(propertyDescriptor.getName(), dto), ex);
        }
    }

    @Override
    public void set(final Object dto, final @Nullable Object value) {
        try {
            propertyDescriptor.getWriteMethod().invoke(dto, value);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to set property '%s' of DTO: %s".formatted(propertyDescriptor.getName(), dto), ex);
        }
    }

    @Override
    public Class<?> type() {
        return propertyDescriptor.getPropertyType();
    }

    @Override
    public Class<?>[] genericTypes() {
        return ClassFieldCache.getGenericTypes(propertyDescriptor.getReadMethod().getGenericReturnType());
    }
}
