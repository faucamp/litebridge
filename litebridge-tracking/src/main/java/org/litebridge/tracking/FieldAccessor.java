package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;

public interface FieldAccessor {

    String name();

    @Nullable
    Object get(final Object dto);

    void set(final Object dto, final @Nullable Object value);

    /**
     * Returns a {@code Class} object that identifies the declared type for the field represented by this {@code FieldAccessor} object.
     *
     * @return a {@code Class} object identifying the declared type of the field represented by this object
     */
    Class<?> type();

    Class<?>[] genericTypes();

    default Class<?> genericType() {
        final Class<?>[] genericTypes = genericTypes();

        if (genericTypes.length != 1) {
            throw new IllegalStateException("Expected exactly one generic type, but got " + genericTypes.length);
        }

        return genericTypes[0];
    }

    Class<?> dtoClass();
}
