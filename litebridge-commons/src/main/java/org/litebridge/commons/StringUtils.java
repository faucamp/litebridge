package org.litebridge.commons;

import org.jspecify.annotations.Nullable;

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

    /**
     * Splits the given string into two parts by the first occurrence of the specified delimiter.
     * If the delimiter is not found, returns an array containing the original string as the only element.
     *
     * @param str       the string to be split; must not be null
     * @param delimiter the character used as the delimiter for the split
     * @return an array of two strings; the part before the delimiter and the part after it.
     * If the delimiter is not found, returns a single-element array containing the original string.
     */
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

    public static String blankIfNull(final @Nullable String str) {
        return str == null ? "" : str;
    }

    public static String lowerFirst(final String str) {
        if (isEmpty(str)) {
            throw new IllegalArgumentException("Empty or null input string");
        }

        final char firstChar = Character.toLowerCase(str.charAt(0));
        return firstChar + str.substring(1);
    }

    public static String camelCase(final String str) {
        if (isEmpty(str)) {
            return str;
        }

        // Split the string by any non-word characters (including spaces and underscores)
        final String[] words = str.split("[\\W_]+");
        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            final String word = words[i];

            if (word.isEmpty()) {
                continue;
            }

            if (i == 0) {
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

    public static boolean isAsciiOnly(final String str) {
        final int len = str.length();

        for (int i = 0; i < len; i++) {
            final char ch = str.charAt(i);

            if (ch > 0x7F) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns a lowercase string consisting of:
     * <ul>
     *     <li>the first letter of each "word" (a letter that follows a non-letter/digit),</li>
     *     <li>the first letter of each camelCase hump (an uppercase letter that follows a lowercase letter),</li>
     *     <li>and all digits found anywhere in the input.</li>
     * </ul>
     * Non-letter/digit characters act as separators and are otherwise ignored.
     */
    public static String abbreviate(final @Nullable String str) {
        if (isEmpty(str)) {
            return "";
        }

        final int len = str.length();

        // Fast path: common case for DB/Java identifiers (ASCII, no surrogates)
        if (isAsciiOnly(str)) {
            return abbreviateAscii(str, len);
        }

        // Unicode-correct path (iterates code points)
        final StringBuilder sb = new StringBuilder(len);
        boolean prevWasLetterOrDigit = false;
        boolean prevWasLowercaseLetter = false;

        for (int i = 0; i < len; ) {
            final int cp = str.codePointAt(i);
            final int step = Character.charCount(cp);

            if (Character.isDigit(cp)) {
                sb.appendCodePoint(cp);
                prevWasLetterOrDigit = true;
                prevWasLowercaseLetter = false;
            } else if (Character.isLetter(cp)) {
                final boolean isWordStartAfterSeparator = !prevWasLetterOrDigit;
                final boolean isCamelHumpStart = Character.isUpperCase(cp) && prevWasLowercaseLetter;

                if (isWordStartAfterSeparator || isCamelHumpStart) {
                    sb.appendCodePoint(Character.toLowerCase(cp));
                }

                prevWasLetterOrDigit = true;
                prevWasLowercaseLetter = Character.isLowerCase(cp);
            } else {
                prevWasLetterOrDigit = false;
                prevWasLowercaseLetter = false;
            }

            i += step;
        }

        return sb.toString();
    }

    private static String abbreviateAscii(final String str, final int len) {
        final StringBuilder sb = new StringBuilder(len);

        boolean prevWasLetterOrDigit = false;
        boolean prevWasLowercaseLetter = false;

        for (int i = 0; i < len; i++) {
            final char ch = str.charAt(i);

            if (ch >= '0' && ch <= '9') {
                sb.append(ch);
                prevWasLetterOrDigit = true;
                prevWasLowercaseLetter = false;
                continue;
            }

            final boolean isLower = (ch >= 'a' && ch <= 'z');
            final boolean isUpper = (ch >= 'A' && ch <= 'Z');

            if (isLower || isUpper) {
                final boolean isWordStartAfterSeparator = !prevWasLetterOrDigit;
                final boolean isCamelHumpStart = isUpper && prevWasLowercaseLetter;

                if (isWordStartAfterSeparator || isCamelHumpStart) {
                    sb.append(isUpper ? (char) (ch + ('a' - 'A')) : ch);
                }

                prevWasLetterOrDigit = true;
                prevWasLowercaseLetter = isLower;
            } else {
                // separator
                prevWasLetterOrDigit = false;
                prevWasLowercaseLetter = false;
            }
        }

        return sb.toString();
    }
}
