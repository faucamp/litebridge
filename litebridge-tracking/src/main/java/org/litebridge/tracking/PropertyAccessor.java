package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.type.ConcurrentLazy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Property-based {@code FieldAccessor} implementation.
 * <p>
 * A concrete implementation of the {@code FieldAccessor} interface that provides access
 * to a property of a data transfer object (DTO) using JavaBeans {@code PropertyDescriptor}.
 * <p>
 * This class allows retrieval and manipulation of a property’s value, as well as access
 * to its metadata, such as name, type, and declaring class.
 */
public final class PropertyAccessor implements FieldAccessor {

    private final MethodHandle getter;
    private final MethodHandle setter;
    private final MethodHandleInfo info;
    private final ConcurrentLazy<Class<?>[]> genericTypes;
    private final int hashCode;

    public PropertyAccessor(final Field field, final MethodHandles.Lookup lookup, final ClassFieldAccessorCache classFieldAccessorCache) {
        try {
            this.getter = lookup.unreflectGetter(field);
            this.setter = lookup.unreflectSetter(field);
            this.info = lookup.revealDirect(getter);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException("Failed to unreflect getter and setter for field: '%s' of class: %s".formatted(field.getName(), field.getDeclaringClass().getName()), ex);
        }

        this.genericTypes = new ConcurrentLazy<>(() -> classFieldAccessorCache.getGenericTypes(field));
        this.hashCode = field.hashCode();
    }

    @Override
    public String name() {
        return info.getName();
    }

    @Override
    public Object get(final Object dto) {
        try {
            return getter.invoke(dto);
        } catch (Throwable ex) {
            throw new IllegalArgumentException("Failed to get property '%s' of DTO: %s".formatted(name(), dto), ex);
        }
    }

    @Override
    public void set(final Object dto, final @Nullable Object value) {
        try {
            setter.invoke(dto, value);
        } catch (Throwable ex) {
            throw new IllegalArgumentException("Failed to set property '%s' of DTO: %s".formatted(name(), dto), ex);
        }
    }

    @Override
    public Class<?> type() {
        return info.getMethodType().returnType();
    }

    @Override
    public Class<?>[] genericTypes() {
        return genericTypes.orThrow();
    }

    @Override
    public Class<?> dtoClass() {
        return info.getDeclaringClass();
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final PropertyAccessor that)) return false;
        return Objects.equals(hashCode, that.hashCode);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PropertyAccessor.class.getSimpleName() + "[", "]")
                .add("property=" + name())
                .add("type=" + type())
                .add("dtoClass=" + dtoClass())
                .toString();
    }
}
