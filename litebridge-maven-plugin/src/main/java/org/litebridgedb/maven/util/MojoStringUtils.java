package org.litebridgedb.maven.util;

import java.util.Objects;

public final class MojoStringUtils {

    private MojoStringUtils() {
    }

    /**
     * Converts the given string into camelCase format by removing non-word characters,
     * Lowercasing the first word if {@code lowercaseFirst} is {@code true},
     * and capitalizing the first letter of subsequent words.
     *
     * @param str the input string to be converted; must not be null
     * @return the camelCase formatted string, or an empty string if the input is empty or contains only non-word characters
     * @throws NullPointerException if the input string is null
     */
    public static String camelCase(final String str, final boolean lowercaseFirst) {
        Objects.requireNonNull(str, "Input cannot be null");

        // Split the string by any non-word characters (including spaces and underscores)
        final String[] words = str.split("[\\W_]+");
        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            final String word = words[i];

            if (lowercaseFirst && i == 0) {
                // For the first word, convert to lowercase
                builder.append(word.toLowerCase());
            } else {
                // For subsequent words, capitalize the first letter and lowercase the rest
                builder.append(Character.toUpperCase(word.charAt(0)));
                builder.append(word.substring(1).toLowerCase());
            }
        }

        return builder.toString();
    }
}
