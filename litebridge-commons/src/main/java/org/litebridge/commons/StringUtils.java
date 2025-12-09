package org.litebridge.commons;

import jakarta.annotation.Nullable;

public final class StringUtils {

    private StringUtils() {
    }

    /**
     * Checks if the given string is null, empty, or contains only whitespace characters.
     *
     * @param str the string to check; may be null
     * @return true if the string is null, empty, or contains only whitespace characters; false otherwise
     */
    public static boolean isBlank(@Nullable final String str) {
        return str == null || str.isBlank();
    }

    /**
     * Checks if the given string is null or empty.
     *
     * @param str the string to check; may be null
     * @return true if the string is null or empty; false otherwise
     */
    public static boolean isEmpty(@Nullable final String str) {
        return str == null || str.isEmpty();
    }

    public static String[] splitOnce(final String str, final char delimiter) {
        final int index = str.indexOf(delimiter);

        if (index == -1) {
            return new String[]{str};
        } else {
            final String part1 = str.substring(0, index);
            final String part2 = str.substring(index + 1);
            return new String[]{part1, part2};
        }
    }

    /**
     * Ensures that the provided string is not null, empty, or composed solely of whitespace characters.
     * If the string is blank, an {@code IllegalArgumentException} is thrown with the provided message.
     *
     * @param str     the string to check; may be null
     * @param message the exception message to use if the string is blank
     * @return the provided string if it is not blank
     * @throws IllegalArgumentException if the string is blank
     */
    public static String requireNonBlank(@Nullable final String str, final String message) {
        if (isBlank(str)) {
            throw new IllegalArgumentException(message);
        }

        return str;
    }
}
