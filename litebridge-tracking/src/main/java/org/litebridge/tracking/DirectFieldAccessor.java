package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.type.ConcurrentLazy;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Direct field access {@code FieldAccessor} implementation using Java 9's {@link VarHandle}.
 * <p>
 * Provides functionality for accessing and manipulating fields of a data transfer object (DTO) directly
 * using Java Reflection.
 * <p>
 * This class enables retrieving field names, types, generic types, and the declaring class.
 * It also supports getting and setting field values within a given DTO instance.
 */
public final class DirectFieldAccessor implements FieldAccessor {

    private final Field field;
    private final VarHandle varHandle;
    private final ConcurrentLazy<Class<?>[]> genericTypes;

    public DirectFieldAccessor(final Field field, final MethodHandles.Lookup lookup) {
        this.field = field;
        try {
            this.varHandle = lookup.unreflectVarHandle(field);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException("Failed to unreflect VarHandle for field: '%s' of class: %s".formatted(field.getName(), field.getDeclaringClass().getName()), ex);
        }
        this.genericTypes = new ConcurrentLazy<>(() -> ClassUtils.getGenericTypes(field));
    }

    @Override
    public String name() {
        return field.getName();
    }

    @Override
    public Object get(final Object dto) {
        try {
            return varHandle.get(dto);
        } catch (final ClassCastException ex) {
            throw new IllegalArgumentException("DTO class does not match field accessor class", ex);
        }
    }

    @Override
    public void set(final Object dto, final @Nullable Object value) {
        varHandle.set(dto, value);
    }

    @Override
    public Class<?> type() {
        return varHandle.varType();
    }

    @Override
    public Class<?>[] genericTypes() {
        return genericTypes.orThrow();
    }

    @Override
    public Class<?> dtoClass() {
        return field.getDeclaringClass();
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final DirectFieldAccessor that)) return false;
        return Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(field);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DirectFieldAccessor.class.getSimpleName() + "[", "]")
                .add("field=" + name())
                .add("type=" + type())
                .add("dtoClass=" + dtoClass())
                .toString();
    }
}
