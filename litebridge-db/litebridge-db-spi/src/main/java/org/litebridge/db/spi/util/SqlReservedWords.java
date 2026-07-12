package org.litebridge.db.spi.util;

import java.util.Set;

/**
 * Utility class for working with SQL reserved words.
 * <p>
 * This class provides functionality to verify if a given word
 * is a reserved keyword in SQL. The list of reserved words
 * is predefined and immutable.
 */
public final class SqlReservedWords {

    private SqlReservedWords() {
    }

    private static final Set<String> reservedWords = Set.of(
            "SELECT",
            "INSERT",
            "UPDATE",
            "DELETE",
            "FROM",
            "WHERE",
            "AND",
            "OR",
            "NOT",
            "CREATE",
            "ALTER",
            "DROP",
            "TABLE",
            "COLUMN",
            "INDEX",
            "JOIN",
            "ON",
            "GROUP",
            "BY",
            "HAVING",
            "ORDER",
            "UNION",
            "ALL",
            "AS",
            "CASE",
            "WHEN",
            "THEN",
            "ELSE",
            "END",
            "NULL",
            "TRUE",
            "FALSE",
            "ASC",
            "DESC"
    );

    /**
     * Checks if the given word is a SQL reserved word.
     *
     * @param word The word to check.
     * @return {@code true} if the word is a reserved word, {@code false} otherwise.
     */
    public static boolean contains(final String word) {
        return reservedWords.contains(word.toUpperCase());
    }
}
