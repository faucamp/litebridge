package org.litebridge.commons;

import org.jspecify.annotations.Nullable;

/**
 * Utility class for boolean operations.
 */
public final class BooleanUtils {

    private BooleanUtils() {
    }

    /**
     * Converts a {@link Boolean} object to a primitive {@code boolean}.
     *
     * @param value the {@link Boolean} object to be converted, which may be {@code null}.
     * @return {@code true} if the input value is {@link Boolean#TRUE}, otherwise {@code false}.
     */
    public static boolean toBoolean(final @Nullable Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    /**
     * Ensures that a given boolean value is {@code true}.
     * If the value is {@code false}, throws an {@link IllegalArgumentException} with the specified message.
     *
     * @param value   the boolean value to check
     * @param message the exception message to use if the value is {@code false}
     * @return {@code true} if the value is {@code true}
     * @throws IllegalArgumentException if the value is {@code false}
     */
    public static boolean requireTrue(final boolean value, final String message) {
        if (!value) {
            throw new IllegalArgumentException(message);
        }

        return true;
    }

    /**
     * Ensures that a given boolean value is {@code false}.
     * If the value is {@code true}, throws an {@link IllegalArgumentException} with the specified message.
     *
     * @param value   the boolean value to check
     * @param message the exception message to use if the value is {@code true}
     * @return {@code false} if the value is {@code false}
     * @throws IllegalArgumentException if the value is {@code true}
     */
    public static boolean requireFalse(final boolean value, final String message) {
        if (value) {
            throw new IllegalArgumentException(message);
        }

        return false;
    }
}
