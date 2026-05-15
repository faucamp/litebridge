package org.litebridgedb.commons;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.function.Supplier;

/**
 * Utility class for working with objects.
 */
public final class ObjectUtils {

    private ObjectUtils() {
    }

    /**
     * Ensures that the provided object is not null.
     * <p>
     * If the object is null, an exception provided by the {@code exceptionSupplier} is thrown.
     *
     * @param <T>               the type of the object to be validated
     * @param <X>               the type of the exception to be thrown if validation fails
     * @param obj               the object to be checked for nullability
     * @param exceptionSupplier the supplier that provides an exception to be thrown if {@code obj} is null
     * @return the non-null object passed as the {@code obj} parameter
     * @throws X if {@code obj} is null
     */
    public static <T, X extends Throwable> T requireNonNull(@Nullable final T obj, final Supplier<? extends X> exceptionSupplier) throws X {
        if (obj == null) {
            throw exceptionSupplier.get();
        }

        return obj;
    }

    /**
     * Ensures that the provided object is {@code null}.
     * <p>
     * If the object is not null, an exception provided by the {@code exceptionSupplier} is thrown.
     *
     * @param obj               the object to be checked for nullity
     * @param exceptionSupplier the supplier that provides an exception to be thrown if {@code obj} is not null
     * @param <X>               the type of the exception to be thrown
     * @throws X if {@code obj} is not null
     */
    public static <X extends Throwable> void requireNull(@Nullable final Object obj, final Supplier<? extends X> exceptionSupplier) throws X {
        if (obj != null) {
            throw exceptionSupplier.get();
        }
    }

    /**
     * Retrieves the value of a specified field from the given object.
     * <p>
     * The field is accessed reflectively, and its value is returned as an object of the specified type.
     *
     * @param <T>       the expected type of the field's value
     * @param dto       the object from which the field value is to be retrieved
     * @param fieldName the name of the field to retrieve
     * @param fieldType the expected class type of the field's value
     * @return the value of the specified field, cast to the specified type
     * @throws IllegalStateException if the field is inaccessible or retrieval fails
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(final Object dto, final String fieldName, final Class<T> fieldType) {
        final Field field = ClassUtils.getField(dto.getClass(), fieldName);
        field.setAccessible(true);
        try {
            return (T) field.get(dto);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to get field " + field + " on object: " + dto);
        }
    }
}
