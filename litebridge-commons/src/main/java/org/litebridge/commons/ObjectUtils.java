package org.litebridge.commons;

import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public final class ObjectUtils {

    private ObjectUtils() {
    }

    /**
     * Ensures that the provided object is not null.
     * If the object is null, an IllegalArgumentException is thrown with the given message.
     *
     * @param obj     the object to be checked for nullability
     * @param message the exception message to be used if the object is null
     * @param <T>     the type of the object to be validated
     * @return the non-null object passed as the first parameter
     * @throws IllegalArgumentException if {@code obj} is null
     */
    public static <T> T requireNonNull(@Nullable final T obj, @Nullable final String message) throws IllegalArgumentException {
        return requireNonNull(obj, () -> {
            if (!StringUtils.isEmpty(message)) {
                return new IllegalArgumentException(message);
            } else {
                return new IllegalArgumentException();
            }
        });
    }

    public static <T, X extends Throwable> T requireNonNull(@Nullable final T obj, final Supplier<? extends X> exceptionSupplier) throws X {
        if (obj == null) {
            throw exceptionSupplier.get();
        }

        return obj;
    }

    public static <T, X extends Throwable> T requireNull(@Nullable final T obj, final Supplier<? extends X> exceptionSupplier) throws X {
        if (obj != null) {
            throw exceptionSupplier.get();
        }

        return obj;
    }
}
