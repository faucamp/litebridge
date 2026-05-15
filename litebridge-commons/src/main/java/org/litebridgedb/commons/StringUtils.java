package org.litebridgedb.commons;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for working with strings.
 */
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
     * Splits a string by a single character. This is significantly faster than
     * String.split() for single-character delimiters because it avoids regex.
     *
     * @param target    The string to split.
     * @param delimiter The character to split by.
     * @return A list of strings.
     */
    public static List<String> split(@Nullable String target, char delimiter) {
        return split(target, delimiter, -1, false);
    }

    /**
     * Splits the given string by the specified delimiter, with optional padding and size constraints.
     *
     * @param target          The string to split; may be null. If null, an empty list is returned.
     * @param delimiter       The character to use as the delimiter for splitting the string.
     * @param setSize         The desired number of elements in the result. If greater than 0,
     *                        the resulting list will be padded or validated to match this size.
     *                        If padding is required, empty strings will be used.
     * @param padEmptyAtStart If true, padding is added at the start of the list to meet the required size.
     *                        If false, padding is added at the end of the list.
     * @return A list of substrings resulting from the split. If the setSize parameter is provided and
     * greater than 0, the list will be adjusted to match the desired size. If the input string
     * is null, an empty list is returned. Throws {@code IllegalArgumentException} if the number
     * of segments exceeds the specified setSize.
     */
    public static List<String> split(@Nullable String target, char delimiter, final int setSize, final boolean padEmptyAtStart) {
        if (target == null) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        int start = 0;
        int end = target.indexOf(delimiter);

        while (end != -1) {
            result.add(target.substring(start, end));
            start = end + 1;
            end = target.indexOf(delimiter, start);
        }

        // Add the final segment
        result.add(target.substring(start));

        if (setSize > 0) {
            if (result.size() < setSize) {
                if (padEmptyAtStart) {
                    result.addAll(0, Collections.nCopies(setSize - result.size(), ""));
                } else {
                    result.addAll(Collections.nCopies(setSize - result.size(), ""));
                }
            } else if (result.size() > setSize) {
                throw new IllegalArgumentException("Could not parse string: '" + target + "'; expected " + setSize + " parts, got: " + result.size());
            }
        }

        return result;
    }


    /**
     * Splits a given string into an array of substrings based on a specified delimiter,
     * with optional size constraints and padding for the resulting array.
     *
     * @param target          The string to split; may be null. If null, an empty array is returned.
     * @param delimiter       The character used as the delimiter for splitting the string.
     * @param setSize         The desired number of elements in the result. If greater than 0,
     *                        the resulting array will be padded or validated to match this size.
     *                        If padding is required, empty strings will be used.
     * @param padEmptyAtStart If true, padding is added at the start of the array to meet the required size.
     *                        If false, padding is added at the end of the array.
     * @return An array of substrings resulting from the split. If the setSize parameter is provided
     * and greater than 0, the array will be adjusted to match the desired size.
     * If the input string is null, an empty array is returned.
     * @throws IllegalArgumentException if the number of segments exceeds the specified setSize.
     */
    public static String[] splitArray(@Nullable String target, char delimiter, final int setSize, final boolean padEmptyAtStart) {
        return split(target, delimiter, setSize, padEmptyAtStart).toArray(new String[0]);
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

    /**
     * Returns an empty string if the given string is null; otherwise, returns the input string.
     *
     * @param str the input string; may be null
     * @return an empty string if {@code str} is null; otherwise, the input string
     */
    public static String blankIfNull(final @Nullable String str) {
        return str == null ? "" : str;
    }


    /**
     * Checks if the given string contains only ASCII characters.
     * <p>
     * An ASCII character is defined as a character with a value less than or equal to 0x7F.
     *
     * @param str the string to check; must not be null
     * @return true if the string contains only ASCII characters; false otherwise
     */
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
