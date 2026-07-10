package org.litebridgedb.maven.util;

import org.litebridgedb.commons.StringUtils;

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

    public static String lowerFirst(final String name) {
        if (StringUtils.isEmpty(name)) {
            return name;
        }

        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    public static String pluralise(final String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        // Words ending in y preceded by a consonant (e.g. company -> companies, category -> categories)
        // but NOT preceded by a vowel (e.g., day -> days, key -> keys)
        if (name.endsWith("y") && name.length() > 1) {
            char prev = name.charAt(name.length() - 2);
            if (prev != 'a' && prev != 'e' && prev != 'i' && prev != 'o' && prev != 'u') {
                return name.substring(0, name.length() - 1) + "ies";
            }
        }

        // Words ending in s, sh, ch, x, or z need "es" (e.g. bus -> buses, wish -> wishes, bench -> benches, box -> boxes)
        if (name.endsWith("s") || name.endsWith("sh") || name.endsWith("ch") || name.endsWith("x") || name.endsWith("z")) {
            return name + "es";
        }

        // Default rule: just add s (e.g. account -> accounts, user -> users)
        return name + "s";
    }
}
