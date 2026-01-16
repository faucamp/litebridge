package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;

import java.beans.PropertyDescriptor;
import java.util.Objects;

/**
 * Property-based {@code FieldAccessor} implementation.
 * <p>
 * A concrete implementation of the {@code FieldAccessor} interface that provides access
 * to a property of a data transfer object (DTO) using JavaBeans {@code PropertyDescriptor}.
 * <p>
 * This class allows retrieval and manipulation of a property’s value, as well as access
 * to its metadata, such as name, type, and declaring class.
 */
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
            throw new IllegalArgumentException("Failed to get property '%s' of DTO: %s".formatted(propertyDescriptor.getName(), dto), ex);
        }
    }

    @Override
    public void set(final Object dto, final @Nullable Object value) {
        try {
            propertyDescriptor.getWriteMethod().invoke(dto, value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to set property '%s' of DTO: %s".formatted(propertyDescriptor.getName(), dto), ex);
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

    @Override
    public Class<?> dtoClass() {
        return propertyDescriptor.getReadMethod().getDeclaringClass();
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final PropertyAccessor that)) return false;
        return Objects.equals(propertyDescriptor, that.propertyDescriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(propertyDescriptor);
    }
}
