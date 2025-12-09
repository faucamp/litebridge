package org.litebridge.commons;

public final class ObjectUtils {

    private ObjectUtils() {
    }

    /**
     * Ensures that the provided object is not null.
     * If the object is null, an IllegalArgumentException is thrown with the given message.
     *
     * @param obj the object to be checked for nullability
     * @param message the exception message to be used if the object is null
     * @return the non-null object passed as the first parameter
     * @param <T> the type of the object to be validated
     * @throws IllegalArgumentException if {@code obj} is null
     */
    public static <T> T requireNonNull(final T obj, final String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }

        return obj;
    }
}
