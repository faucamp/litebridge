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
}
